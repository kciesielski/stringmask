import sbt.Keys._

import scalariform.formatter.preferences._

lazy val commonSettings = scalariformSettings ++ Seq(
    organization := "com.softwaremill.stringmask",
    version := "2.0.0-SNAPSHOT",
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
    scalaVersion := "2.11.8",
    ScalariformKeys.preferences in ThisBuild := ScalariformKeys.preferences.value
        .setPreference(DoubleIndentClassDeclaration, true)
        .setPreference(PreserveSpaceBeforeArguments, true)
        .setPreference(CompactControlReadability, true)
        .setPreference(SpacesAroundMultiImports, false)
)

lazy val root = (project in file("."))
    .aggregate(annotation, scalacPlugin, tests)
    .settings(commonSettings)
    .settings(name := "scalac-stringmask-plugin")


lazy val annotation = (project in file("annotation"))
    .settings(commonSettings)
    .settings(name := "stringmask-annotation")
    .settings(publishArtifact := true)

lazy val scalacPlugin = (project in file("scalacPlugin"))
    .dependsOn(annotation)
    .settings(commonSettings)
    .settings(
        name := "stringmask-scalac-plugin",
        exportJars := true
    )
    .settings(
        libraryDependencies ++= Seq(
            "org.scala-lang" % "scala-compiler" % scalaVersion.value
        )
    )

lazy val tests = (project in file("tests"))
    .dependsOn(scalacPlugin)
    .settings(commonSettings)
    .settings(
        scalacOptions <+= (artifactPath in(scalacPlugin, Compile, packageBin)).map { file =>
            s"-Xplugin:${file.getAbsolutePath}"
        }
    ).settings(
    libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.2.6"
    )
)

publishMavenStyle in ThisBuild := true
publishArtifact in ThisBuild := true
publishArtifact in Test in ThisBuild := false
pomIncludeRepository := { _ => false }
pomExtra in ThisBuild :=
    <scm>
        <url>git@github.com:softwaremill/stringmask.git</url>
        <connection>scm:git:git@github.com:softwaremill/stringmask.git</connection>
    </scm>
        <developers>
            <developer>
                <id>kciesielski</id>
                <name>Krzysztof Ciesielski</name>
            </developer>
            <developer>
                <id>mkubala</id>
                <name>Marcin Kubala</name>
            </developer>
        </developers>

licenses in ThisBuild := ("Apache2", new java.net.URL("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil
homepage in ThisBuild := Some(new java.net.URL("http://www.softwaremill.com"))
