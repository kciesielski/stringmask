package com.softwaremill.stringmask

import com.softwaremill.stringmask.components.StringMaskComponent

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.{ Plugin, PluginComponent }

class StringMaskPlugin(val global: Global) extends Plugin {

  override val name: String = "stringmask"
  override val description: String = "StringMask compiler plugin"
  override val components: List[PluginComponent] = List(new StringMaskComponent(global))
}
