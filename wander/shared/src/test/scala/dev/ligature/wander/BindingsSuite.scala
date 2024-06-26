/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.wander

import dev.ligature.wander.parser.{LigatureValue, Name}
import dev.ligature.{Identifier, StringLiteral}
import munit.FunSuite

class BindingsSuite extends FunSuite {
  val identifier = Name("test")
  val identifier2 = Name("test2")

  val value1 = LigatureValue(StringLiteral("this is a test"))
  val value2 = LigatureValue(StringLiteral("this is a test2"))
  val value3 = LigatureValue(StringLiteral("this is a test3"))

  test("add single value and read") {
    val binding = Bindings()
    val binding2 = binding.bindVariable(identifier, value1).getOrElse(???)
    val res = binding.read(identifier)
    val res2 = binding2.read(identifier)

    assert(res.isLeft)
    assert(binding.read(identifier2).isLeft)
    assertEquals(res2, Right(value1))
    assert(binding2.read(identifier2).isLeft)
  }

  test("test scoping") {
    val bindings = Bindings()
    val bindings2 = bindings.bindVariable(identifier, value1).getOrElse(???)
    assertEquals(bindings2.read(identifier), Right(value1))

    val bindings3 = bindings2.newScope()
    assertEquals(bindings3.read(identifier), Right(value1))

    val bindings4 = bindings3.bindVariable(identifier, value2).getOrElse(???)
    val bindings5 = bindings4.bindVariable(identifier2, value3).getOrElse(???)
    assertEquals(bindings5.read(identifier), Right(value2))
    assertEquals(bindings5.read(identifier2), Right(value3))
  }
}
