/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.wander

import dev.ligature.wander.parse
import scala.annotation.unused
import dev.ligature.wander.preludes.common

/** Represents a Value in the Wander language.
  */
enum WanderValue:
  case Nothing
  case IntValue(value: Long)
  case BooleanValue(value: Boolean)
  case StringValue(value: String)
  case Identifier(value: dev.ligature.wander.Identifier)
  case Array(values: Seq[WanderValue])
  case Lambda(lambda: Expression.Lambda)
  case HostFunction(hostFunction: dev.ligature.wander.HostFunction)
  case HostProperty(hostProperty: dev.ligature.wander.HostProperty)
  case QuestionMark

case class HostFunction(
    name: String,
    fn: (
        arguments: Seq[Expression],
        bindings: Environment
    ) => Either[WanderError, (WanderValue, Environment)]
)

case class HostProperty(
    name: String,
    read: (
        bindings: Environment
    ) => Either[WanderError, (WanderValue, Environment)]
)

case class Parameter(
    name: Name,
    parameterType: Option[WanderValue]
)

case class WanderError(val userMessage: String) extends Throwable(userMessage)

def run(
    script: String,
    environment: Environment
): Either[WanderError, (WanderValue, Environment)] =
  val expression = for {
    tokens <- tokenize(script)
    terms <- parse(tokens)
    expression <- process(terms)
  } yield expression
  expression match
    case Left(value)  => Left(value)
    case Right(value) => environment.eval(value)

case class Introspect(
    tokens: Either[WanderError, Seq[Token]],
    terms: Either[WanderError, Seq[Term]],
    expression: Either[WanderError, Seq[Expression]]
)

def introspect(script: String): Introspect = {
  val tokens = tokenize(script)

  val terms = if (tokens.isRight) {
    parse(tokens.getOrElse(???))
  } else {
    Left(WanderError("Previous error."))
  }

  val expression = if (terms.isRight) {
    process(terms.getOrElse(???))
  } else {
    Left(WanderError("Previous error."))
  }

  Introspect(tokens, terms, expression)
}

def printResult(value: Either[WanderError, (WanderValue, Environment)]): String =
  value match {
    case Left(value)  => "Error: " + value.userMessage
    case Right(value) => printWanderValue(value._1)
  }

def printWanderValue(value: WanderValue): String =
  value match {
    case WanderValue.QuestionMark => "?"
    case WanderValue.BooleanValue(value)   => value.toString()
    case WanderValue.IntValue(value)       => value.toString()
    case WanderValue.StringValue(value)    => s"\"value\"" // TODO escape correctly
    case WanderValue.Identifier(value)     => s"<${value.name}>"
    case WanderValue.HostFunction(body)    => "[HostFunction]"
    case WanderValue.HostProperty(propety) => "[HostProperty]" // TODO print value?
    case WanderValue.Nothing               => "nothing"
    case WanderValue.Lambda(lambda)        => "[Lambda]"
    case WanderValue.Array(values) =>
      "[" + values.map(value => printWanderValue(value)).mkString(", ") + "]"
  }

final case class Identifier private (name: String) {
  @unused
  private def copy(): Unit = ()
}

object Identifier {
  private val pattern = "^[a-zA-Z0-9-._~:/?#\\[\\]@!$&'()*+,;%=]+$".r

  def fromString(name: String): Either[WanderError, Identifier] =
    if (pattern.matches(name)) {
      Right(Identifier(name))
    } else {
      Left(WanderError(s"Invalid Identifier $name"))
    }
}