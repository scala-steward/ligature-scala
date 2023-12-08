/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.wander

import dev.ligature.wander.*
import scala.collection.mutable.ListBuffer
import scala.util.boundary, boundary.break

enum Expression:
  case NameExpression(value: Name)
  case IdentifierValue(value: Identifier)
  case IntegerValue(value: Long)
  case StringValue(value: String)
  case BooleanValue(value: Boolean)
  case Nothing
  case Array(value: Seq[Expression])
  case LetExpression(name: Name, value: Expression)
  case Lambda(parameters: Seq[Name], body: Expression)
  case WhenExpression(conditionals: Seq[(Expression, Expression)])
  case Application(expressions: Seq[Expression])
  case Grouping(expressions: Seq[Expression])
  case QuestionMark

def eval(
    expressions: Seq[Expression],
    environment: Environment
): Either[WanderError, (WanderValue, Environment)] =
  var lastResult = WanderValue.Nothing
  boundary:
    expressions.foreach(expression =>
      eval(expression, environment) match {
        case Right(value) => lastResult = value._1
        case Left(err)    => break(Left(err))
      }
    )
  Right((lastResult, environment))

def eval(
    expression: Expression,
    environment: Environment
): Either[WanderError, (WanderValue, Environment)] =
  expression match {
    case Expression.Nothing             => Right((WanderValue.Nothing, environment))
    case Expression.BooleanValue(value) => Right((WanderValue.BooleanValue(value), environment))
    case Expression.IntegerValue(value) => Right((WanderValue.IntValue(value), environment))
    case Expression.StringValue(value)  => Right((WanderValue.StringValue(value), environment))
    case Expression.IdentifierValue(value) => Right((WanderValue.Identifier(value), environment))
    case Expression.Array(value)           => handleArray(value, environment)
    case Expression.NameExpression(name)   => environment.read(name).map((_, environment))
    case Expression.LetExpression(name, value) => handleLetExpression(name, value, environment)
    case lambda: Expression.Lambda             => Right((WanderValue.Lambda(lambda), environment))
    case Expression.WhenExpression(conditionals) =>
      handleWhenExpression(conditionals, environment)
    case Expression.Grouping(expressions)    => handleGrouping(expressions, environment)
    case Expression.Application(expressions) => handleApplication(expressions, environment)
    case Expression.QuestionMark             => Right((WanderValue.QuestionMark, environment))
  }

def handleGrouping(
    expressions: Seq[Expression],
    environment: Environment
): Either[WanderError, (WanderValue, Environment)] = {
  var error: Option[WanderError] = None
  var res: (WanderValue, Environment) = (WanderValue.Nothing, environment)
  val itr = expressions.iterator
  while error.isEmpty && itr.hasNext do
    eval(itr.next(), res._2) match {
      case Left(err)    => error = Some(err)
      case Right(value) => res = value
    }
  if error.isDefined then Left(error.get)
  else Right(res)
}

def handleLetExpression(
    name: Name,
    value: Expression,
    environment: Environment
): Either[WanderError, (WanderValue, Environment)] = {
  var newScope = environment.newScope()
  eval(value, newScope) match {
    case Left(value) => ???
    case Right(value) =>
      newScope = newScope.bindVariable(name, value._1)
      Right((value._1, newScope))
  }
}

def handleApplication(
    expression: Seq[Expression],
    environment: Environment
): Either[WanderError, (WanderValue, Environment)] =
  expression.head match {
    case Expression.NameExpression(name) =>
      environment.read(name) match {
        case Left(err) => Left(err)
        case Right(value) =>
          val arguments = expression.tail
          value match {
            case WanderValue.Lambda(Expression.Lambda(parameters, body)) =>
              var fnScope = environment.newScope()
              assert(arguments.size == parameters.size)
              parameters.zipWithIndex.foreach { (param, index) =>
                val argument = eval(arguments(index), environment) match {
                  case Left(value) => ???
                  case Right(value) =>
                    fnScope = fnScope.bindVariable(param, value._1)
                }
              }
              eval(body, fnScope)
            case WanderValue.HostFunction(fn) => fn.fn(arguments, environment)
            case _ => Left(WanderError(s"Could not call function ${name.name}."))
          }
      }
    case _ => ???
  }

def handleWhenExpression(
    conditionals: Seq[(Expression, Expression)],
    environment: Environment
): Either[WanderError, (WanderValue, Environment)] =
  boundary:
    conditionals.find { (conditional, _) =>
      eval(conditional, environment) match {
        case Right((value, _)) =>
          value match {
            case WanderValue.BooleanValue(value) => value
            case _ => break(Left(WanderError("Conditionals must evaluate to Bool.")))
          }
        case Left(err) => break(Left(err))
      }
    } match {
      case None            => Left(WanderError("No matching cases."))
      case Some((_, body)) => eval(body, environment)
    }

def handleArray(
    expressions: Seq[Expression],
    environment: Environment
): Either[WanderError, (WanderValue.Array, Environment)] = {
  val res = ListBuffer[WanderValue]()
  val itre = expressions.iterator
  var continue = true
  while continue && itre.hasNext
  do
    val expression = itre.next()
    eval(expression, environment) match
      case Left(err)    => return Left(err)
      case Right(value) => res += value._1
  Right((WanderValue.Array(res.toList), environment))
}
