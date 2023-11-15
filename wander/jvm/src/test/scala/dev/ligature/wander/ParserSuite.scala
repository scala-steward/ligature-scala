/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.wander

import dev.ligature.wander.WanderValue
import munit.FunSuite

class ParserSuite extends FunSuite {
  def ident(identifier: String): Term =
    Identifier.fromString(identifier) match
      case Left(value) => ??? //just crash
      case Right(value) => Term.IdentifierLiteral(value)

  def check(script: String): Either[WanderError, Seq[Term]] =
    val tokens = tokenize(script) match
      case Left(err) => return Left(err)
      case Right(tokens) => tokens
    parse(tokens)

  test("parse Application") {
    val input = check("testing 1 2 3")
    val expected = Right(Seq(Term.Application(Seq(Term.NameTerm(Name("testing")), Term.IntegerLiteral(1), Term.IntegerLiteral(2), Term.IntegerLiteral(3)))))
    assertEquals(input, expected)
  }
  test("parse nothing keyword") {
    val result = check("nothing")
    val expected = Right(Seq(Term.NothingLiteral))
    assertEquals(result, expected)
  }
  test("parse Identifier") {
    val result = check("<test2>")
    val expected = Right(Seq(ident("test2")))
    assertEquals(result, expected)
  }
  test("parse Integer") {
    val result = check("-321")
    val expected = Right(Seq(Term.IntegerLiteral(-321)))
    assertEquals(result, expected)
  }
  test("parse String") {
    val result = check("\"hello\"")
    val expected = Right(Seq(Term.StringLiteral("hello")))
    assertEquals(result, expected)
  }
  test("parse Boolean") {
    val result = check("false")
    val expected = Right(Seq(Term.BooleanLiteral(false)))
    assertEquals(result, expected)
  }
  test("parse Function Calls") {
    val input = check("not true")
    val expected = Right(Seq(Term.Application(Seq(Term.NameTerm(Name("not")), Term.BooleanLiteral(true)))))
    assertEquals(input, expected)
  }
  test("parse Function Call with question mark argument") {
    val result = check("query ? ? ?")
    val expected = Right(Seq(Term.Application(Seq(Term.NameTerm(Name("query")), Term.QuestionMark, Term.QuestionMark, Term.QuestionMark))))
    assertEquals(result, expected)
  }
  test("parse empty List") {
    val result = check("[]")
    val expected = Right(Seq(Term.Array(Seq())))
    assertEquals(result, expected)
  }
  test("parse List") {
    val result = check("[1 2 \"three\"]")
    val expected = Right(Seq(Term.Array(Seq(Term.IntegerLiteral(1), Term.IntegerLiteral(2), Term.StringLiteral("three")))))
    assertEquals(result, expected)
  }
  test("parse let expression") {
    val result = check("let x = 5 in x end")
    val expected = Right(Seq(Term.LetExpression(Seq((Name("x"), Term.IntegerLiteral(5))), Term.NameTerm(Name("x")))))
    assertEquals(result, expected)
  }
  test("parse conditionals") {
    val result = check("if true false else true")
    val expected = Right(Seq(
      Term.IfExpression(
        Term.BooleanLiteral(true), 
        Term.BooleanLiteral(false),
        Term.BooleanLiteral(true)
      )))
    assertEquals(result, expected)
  }
  test("parse Lambda") {
    val result = check("\\x -> x")
    val expected = Right(Seq(
      Term.Lambda(Seq(Name("x")), Seq(Term.NameTerm(Name("x"))))
    ))
    assertEquals(result, expected)
  }
  test("parse empty Record") {
    val result = check("{}")
    val expected = Right(Seq(Term.Record(Seq())))
    assertEquals(result, expected)
  }
  test("parse empty Record") {
    val result = check("{x = 5}")
    val expected = Right(Seq(Term.Record(Seq((Name("x"), Term.IntegerLiteral(5))))))
    assertEquals(result, expected)
  }
}