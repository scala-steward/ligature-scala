/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.dlig

import munit.FunSuite
import dev.ligature.{Identifier, IntegerLiteral, Statement, StringLiteral}
import dev.ligature.gaze.Gaze
import dev.ligature.lig.CommonSuite

class DLigSuite extends CommonSuite(readDLig) {

  test("copy character test with entity and attribute") {
    val input = "<e> <a> 234\n^ ^ 432"
    val expected = List(
      Statement(
        Identifier.fromString("e").getOrElse(???),
        Identifier.fromString("a").getOrElse(???),
        IntegerLiteral(234)
      ),
      Statement(
        Identifier.fromString("e").getOrElse(???),
        Identifier.fromString("a").getOrElse(???),
        IntegerLiteral(432)
      )
    )
    val res = readDLig(input)
    res match {
      case Right(statements) => assertEquals(statements, expected)
      case Left(err)         => fail("failed", clues(err))
    }
  }

  test("error copy character test") {
    val input = "<this:is:an:error> <a> ^"
    val res = readDLig(input)
    assert(res.isLeft)
  }

  test("copy character test with attribute and value") {
    val input = "<e> <a> 234\n<e2> ^ ^"
    val expected = List(
      Statement(
        Identifier.fromString("e").getOrElse(???),
        Identifier.fromString("a").getOrElse(???),
        IntegerLiteral(234)
      ),
      Statement(
        Identifier.fromString("e2").getOrElse(???),
        Identifier.fromString("a").getOrElse(???),
        IntegerLiteral(234)
      )
    )
    val result = readDLig(input)
    result match {
      case Right(statements) => assertEquals(statements, expected)
      case Left(err)         => fail("failed", clues(err))
    }
  }

  test("prefix error test") {
    val input = "prefix x = this:\nx x:is:a x:prefix"
    val result = readDLig(input)
    assert(result.isLeft)
  }

  test("error prefix test") {
    val input = "x x:is:an x:error"
    val result = readDLig(input)
    assert(result.isLeft)
  }

  test("basic prefix test") {
    val input = "prefix x = this:\nx:hello x:cruel x:world"
    val result = readDLig(input)
    result match {
      case Right(statements) => {
        assertEquals(statements.length, 1)
        assertEquals(
          statements(0).entity,
          Identifier.fromString("this:hello").getOrElse(???)
        )
        assertEquals(
          statements(0).attribute,
          Identifier.fromString("this:cruel").getOrElse(???)
        )
        assertEquals(
          statements(0).value,
          Identifier.fromString("this:world").getOrElse(???)
        )
      }
      case Left(err) => fail("failed", clues(err))
    }
  }

  test("entity gen id prefix test") {
    val input = "prefix x = this:\nx:hello{} x:cruel x:world"
    val result = readDLig(input)
    result match {
      case Right(statements) => {
        assertEquals(statements.length, 1)
//        assertEquals(statements(0).entity, Identifier.fromString("this:hello").getOrElse(???))
        assertEquals(
          statements(0).attribute,
          Identifier.fromString("this:cruel").getOrElse(???)
        )
        assertEquals(
          statements(0).value,
          Identifier.fromString("this:world").getOrElse(???)
        )
      }
      case Left(err) => fail("failed", clues(err))
    }
  }

  test("complex prefix test") {
    val input = "prefix x = this:\nx:{} x:{}is:a x:prefix{}"
    val result = readDLig(input)
    result match {
      case Right(statements) => {
        assertEquals(statements.length, 1)
        // TODO add more checks
      }
      case Left(err) => fail("failed", clues(err))
    }
  }
}
