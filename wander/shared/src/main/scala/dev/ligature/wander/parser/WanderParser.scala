/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.wander.parser

import dev.ligature.gaze.{
  Gaze,
  Nibbler,
  optional,
  take,
  takeAll,
  takeCond,
  takeFirst,
  takeString,
  repeat
}
import dev.ligature.{IntegerLiteral, Identifier, StringLiteral}
import dev.ligature.wander.lexer.{Token, TokenType}

def parse(script: Seq[Token]): Either[String, Script] = {
  val filteredInput = script.filter { (token: Token) =>
    token.tokenType != TokenType.Comment && token.tokenType != TokenType.Spaces && token.tokenType != TokenType.NewLine
  }.toVector
  val gaze = Gaze(filteredInput.toVector)
  val res = gaze.attempt(scriptNib)
  res match {
    case None => {
      if (gaze.isComplete()) {
        Right(Script(Seq()))
      } else {
        Left("NoMatch")
      }
    }
    // TODO some case also needs to check if gaze is complete
    case Some(res) => Right(Script(res)) // .filter(_.isDefined).map(_.get)))
  }
}

val booleanNib: Nibbler[Token, Expression] = takeCond[Token] {
  _.tokenType == TokenType.Boolean
}.map { token => Seq(BooleanValue(token(0).content.toBoolean)) }

val identifierNib: Nibbler[Token, Expression] = takeCond[Token] {
  _.tokenType == TokenType.Identifier
}.map { token =>
  Seq(LigatureValue(Identifier.fromString(token(0).content).getOrElse(???)))
}

val integerNib: Nibbler[Token, Expression] = takeCond[Token] {
  _.tokenType == TokenType.Integer
}.map { token => Seq(LigatureValue(IntegerLiteral(token(0).content.toInt))) }

val stringNib: Nibbler[Token, Expression] = takeCond[Token] {
  _.tokenType == TokenType.String
}.map { token => Seq(LigatureValue(StringLiteral(token(0).content))) }

val nameNib = takeCond[Token] {
  _.tokenType == TokenType.Name
}.map { token =>
  Seq(Name(token(0).content))
}

val openBraceNib = takeCond[Token] { _.tokenType == TokenType.OpenBrace }.map {
  token => Seq(OpenBrace)
}

val closeBraceNib =
  takeCond[Token] { _.tokenType == TokenType.CloseBrace }.map { token =>
    Seq(CloseBrace)
  }

val openParenNib = takeCond[Token] { _.tokenType == TokenType.OpenParen }.map {
  token => Seq(OpenParen)
}

val closeParenNib =
  takeCond[Token] { _.tokenType == TokenType.CloseParen }.map { token =>
    Seq(CloseParen)
  }

val arrowNib = takeCond[Token] { _.tokenType == TokenType.Arrow }.map { token =>
  Seq(Arrow)
}

val scopeNib: Nibbler[Token, Scope] = { (gaze) =>
  for {
    _ <- gaze.attempt(openBraceNib)
    expression <- gaze.attempt(optional(repeat(elementNib)))
    _ <- gaze.attempt(closeBraceNib)
  } yield Seq(Scope(expression.toList))
}

val parameterNib: Nibbler[Token, Parameter] = { (gaze) =>
  gaze.attempt(nameNib).map { name => name.map(name => Parameter(name)) }
}

val wanderFunctionNib: Nibbler[Token, FunctionDefinition] = { (gaze) =>
  for {
    _ <- gaze.attempt(openParenNib)
    parameters <- gaze.attempt(optional(repeat(parameterNib)))
    _ <- gaze.attempt(closeParenNib)
    _ <- gaze.attempt(arrowNib)
    body <- gaze.attempt(scopeNib)
  } yield Seq(WanderFunction(parameters.toList, body(0)))
}

val ifKeywordNib =
  takeCond[Token] { _.tokenType == TokenType.IfKeyword }.map { token =>
    Seq(LetKeyword)
  }

val elseKeywordNib =
  takeCond[Token] { _.tokenType == TokenType.ElseKeyword }.map { token =>
    Seq(LetKeyword)
  }

val elseIfExpressionNib: Nibbler[Token, ElseIf] = { (gaze) =>
  for {
    _ <- gaze.attempt(elseKeywordNib)
    _ <- gaze.attempt(ifKeywordNib)
    condition <- gaze.attempt(expressionNib)
    body <- gaze.attempt(expressionNib)
  } yield Seq(ElseIf(condition(0), body(0)))
}

val elseExpressionNib: Nibbler[Token, Else] = { (gaze) =>
  for {
    _ <- gaze.attempt(elseKeywordNib)
    body <- gaze.attempt(expressionNib)
  } yield Seq(Else(body(0)))
}

val ifExpressionNib: Nibbler[Token, IfExpression] = { (gaze) =>
  for {
    _ <- gaze.attempt(ifKeywordNib)
    condition <- gaze.attempt(expressionNib)
    body <- gaze.attempt(expressionNib)
    elseIfs <- gaze.attempt(optional(repeat(elseIfExpressionNib)))
    `else` <- gaze.attempt(optional(elseExpressionNib))
  } yield Seq(
    IfExpression(
      condition(0),
      body(0),
      elseIfs.toList,
      `else`.toList.find(_ => true)
    )
  )
}

val functionCallNib: Nibbler[Token, FunctionCall] = { (gaze) =>
  for {
    name <- gaze.attempt(nameNib)
    _ <- gaze.attempt(openParenNib)
    parameters <- gaze.attempt(optional(repeat(expressionNib)))
    _ <- gaze.attempt(closeParenNib)
  } yield Seq(FunctionCall(name(0), parameters.toList))
}

val expressionNib =
  takeFirst(
    ifExpressionNib,
    functionCallNib,
    nameNib,
    scopeNib,
    identifierNib,
    wanderFunctionNib,
    stringNib,
    integerNib,
    booleanNib
  )

val equalSignNib = takeCond[Token] { _.tokenType == TokenType.EqualSign }.map {
  token => Seq(EqualSign)
}

val letKeywordNib =
  takeCond[Token] { _.tokenType == TokenType.LetKeyword }.map { token =>
    Seq(LetKeyword)
  }

val letStatementNib: Nibbler[Token, LetStatement] = { (gaze) =>
  {
    for {
      _ <- gaze.attempt(letKeywordNib)
      name <- gaze.attempt(nameNib)
      _ <- gaze.attempt(equalSignNib)
      expression <- gaze.attempt(expressionNib)
    } yield Seq(LetStatement(name(0), expression(0)))
  }
}

val elementNib = takeFirst(expressionNib, letStatementNib)

val scriptNib =
  optional(
    repeat(
      elementNib
    )
  )
