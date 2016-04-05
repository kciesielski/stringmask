package com.softwaremill.stringmask.testing

import com.softwaremill.stringmask.annotation.mask
import org.scalatest.{ Matchers, WordSpec }

class BasicCasesSpec extends WordSpec with Matchers {
  "StringMask" should {
    "not override toString" when {
      "none of the class parameters is annotated with @mask" in {
        case class Planet(name: String)

        Planet("earth").toString should equal("Planet(earth)")
      }

      "@mask annotation is applied, but not in the first parameters list" in {
        case class TrickyCurrying(firstArg: String)(@mask secretKey: String)

        TrickyCurrying("red")("tomato").toString should equal("TrickyCurrying(red)")
      }

      "class is implementing a custom toString method" in {
        case class UserWithCustomToString(name: String, @mask password: String) {
          override def toString: String = s"I love potatoes"
        }

        UserWithCustomToString("James", "secretPass").toString should equal("I love potatoes")
      }

      "applied to regular class" in {
        new TestClasses.FavouriteMug("white", 0.4f, "yerba mate").toString should startWith("com.softwaremill.stringmask.testing.TestClasses$FavouriteMug@")
      }

    }

    "mask confidential fields" when {

      "applied to case classes" in {
        case class CasualUser(name: String, @mask password: String)

        CasualUser("James", "secretPass").toString should equal("CasualUser(James,***)")
      }

    }
  }
}

object TestClasses {

  class FavouriteMug(color: String, volume: Float, @mask content: String)

}
