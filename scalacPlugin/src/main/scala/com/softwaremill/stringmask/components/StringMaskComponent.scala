package com.softwaremill.stringmask.components

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.transform.Transform

class StringMaskComponent(val global: Global) extends PluginComponent with Transform {

  override val phaseName: String = "stringmask"

  override val runsAfter: List[String] = List("parser")

  // When runs after typer phase, might have problems with accessing ValDefs' annotations.
  override val runsRightAfter: Option[String] = Some("parser")

  override protected def newTransformer(unit: global.CompilationUnit): global.Transformer = ToStringMaskerTransformer

  import global._

  object ToStringMaskerTransformer extends Transformer {

    override def transform(tree: global.Tree): global.Tree = {
      val transformedTree = super.transform(tree)
      transformedTree match {
        case classDef: ClassDef if isAnnotatedCaseClass(classDef) =>
          extractParamsAnnotatedWithMask(classDef)
            .map(buildNewToStringTree(classDef.name))
            .map(overrideToStringDef(classDef))
            .getOrElse(transformedTree)
        case oth => transformedTree
      }
    }

    private def isAnnotatedCaseClass(classDef: ClassDef): Boolean =
      classDef.mods.isCase && !containsCustomToStringDef(classDef)

    private def containsCustomToStringDef(classDef: global.ClassDef): Boolean =
      classDef.impl.body.exists {
        case d: DefDef => d.name.decode == "toString"
        case _ => false
      }

    private def extractParamsAnnotatedWithMask(classDef: ClassDef): Option[List[Tree]] =
      classDef.impl.body.collectFirst {
        case d: DefDef if d.name.decode == "<init>" && d.vparamss.headOption.exists(containsMaskedParams) =>
          d.vparamss.headOption.map { firstParamsGroup =>
            firstParamsGroup.foldLeft(List.empty[Tree]) {
              case (accList, fieldTree) =>
                val newFieldTree = if (hasMaskAnnotation(fieldTree)) {
                  Literal(Constant("***"))
                } else {
                  Apply(Select(Ident(fieldTree.name), "toString"), Nil)
                }
                accList :+ newFieldTree
            }
          }.getOrElse(Nil)
      }

    private def containsMaskedParams(params: List[ValDef]): Boolean =
      params.exists(hasMaskAnnotation)

    private def hasMaskAnnotation(param: ValDef): Boolean =
      param.mods.hasAnnotationNamed("mask")

    private def overrideToStringDef(classDef: global.ClassDef)(newToStringImpl: Tree): Tree = {
      val className = classDef.name
      global.inform(s"overriding $className.toString")
      val newBody = newToStringImpl :: classDef.impl.body
      val newImpl = Template(classDef.impl.parents, classDef.impl.self, newBody)
      ClassDef(classDef.mods, className, classDef.tparams, newImpl)
    }

    private def buildNewToStringTree(className: TypeName)(fields: List[Tree]): Tree = {
      val treesAsTuple = Apply(Select(Ident("scala"), "Tuple" + fields.length), fields)
      val typeNameStrTree = Literal(Constant(className.toString))

      DefDef(Modifiers(Flag.OVERRIDE), "toString": TermName, List(), List(), TypeTree(),
        Apply(Select(typeNameStrTree, "$plus": TermName), List(treesAsTuple)))
    }

  }

}
