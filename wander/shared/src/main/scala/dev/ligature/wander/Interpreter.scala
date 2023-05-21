/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.wander

import dev.ligature.LigatureError
import dev.ligature.LigatureLiteral
import cats.effect.IO
import cats.implicits._

def eval(script: Seq[Term], bindings: Bindings): IO[ScriptResult] = {
  script.foldLeft(IO.pure(EvalResult(WanderValue.Nothing, bindings))) { (lastResult, term) =>
    lastResult.flatMap { result =>
      evalTerm(term, result.bindings)
    }
  }.map(_.result)
}

def evalAll(terms: Seq[Term], bindings: Bindings): IO[Seq[WanderValue]] =
  terms.map { term => evalTerm(term, bindings) }.sequence.map { evalResult => evalResult.map { _.result } }

def evalTerm(term: Term, bindings: Bindings): IO[EvalResult] =
  term match
    case Term.BooleanLiteral(value) =>
      IO.pure(EvalResult(WanderValue.BooleanValue(value), bindings))
    case Term.IdentifierLiteral(value) =>
      IO.pure(EvalResult(WanderValue.LigatureValue(value), bindings))
    case Term.IntegerLiteral(value) =>
      IO.pure(EvalResult(WanderValue.LigatureValue(LigatureLiteral.IntegerLiteral(value)), bindings))
    case Term.StringLiteral(value) =>
      IO.pure(EvalResult(WanderValue.LigatureValue(LigatureLiteral.StringLiteral(value)), bindings))
    case Term.NameTerm(value) =>
      bindings.read(value) match
        case Left(value) => IO.raiseError(value)
        case Right(value) => IO.pure(EvalResult(value, bindings))
    case Term.LetBinding(name, term) =>
      evalTerm(term, bindings).map { value =>
        bindings.bindVariable(name, value.result) match
          case Left(error) => throw error
          case Right(newBindings) =>
            EvalResult(WanderValue.Nothing, newBindings)
      }
    case Term.List(terms) =>
      evalAll(terms, bindings).map { values =>
        EvalResult(WanderValue.ListValue(values), bindings)
      }
    case Term.FunctionCall(name, arguments) =>
      //TODO val evaldArgs = evalArguments(arguments)
      bindings.read(name) match {
        case Left(value) => ???///IO(Left(value))
        case Right(value) =>
          value match {
            case WanderValue.NativeFunction(parameters, body, output) => {
              body(arguments, bindings).map { value => EvalResult(value, bindings) }
            }
            case WanderValue.WanderFunction(parameters, body, output) => ???
            case _ => ???
          }
      }
    case Term.WanderFunction(parameters, body) => {
      ???
    }
    case Term.Scope(terms) =>
      eval(terms, bindings.newScope()).map { x => EvalResult(x, bindings) }