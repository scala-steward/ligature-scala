/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.wander

import dev.ligature.gaze.{
  Gaze,
  Nibbler,
  optional,
  take,
  takeAll,
  takeCond,
  takeFirst,
  takeString,
  takeUntil,
  takeWhile,
  repeat
}
import scala.collection.mutable.ListBuffer

def process(terms: Seq[Term]): Either[WanderError, Expression] = {
  if terms.isEmpty then
    Right(Expression.Nothing)
  else
    process(terms(0))
}

def process(term: Term): Either[WanderError, Expression] =
  term match {
    case Term.NothingLiteral => Right(Expression.Nothing)
    case Term.Pipe => ???
    case Term.QuestionMark => Right(Expression.Nothing)
    case Term.IdentifierLiteral(value) => Right(Expression.IdentifierValue(value))
    case Term.Array(terms) => processArray(terms)
    case Term.Set(terms) => processSet(terms)
    case Term.BooleanLiteral(value) => Right(Expression.BooleanValue(value))
    case Term.Record(decls) => processRecord(decls)
    case Term.LetExpression(decls, body) => processLetExpression(decls, body)
    case Term.IntegerLiteral(value) => Right(Expression.IntegerValue(value))
    case Term.NameTerm(value) => Right(Expression.NameExpression(value))
    case Term.StringLiteral(value) => Right(Expression.StringValue(value))
    case Term.Application(terms) => processApplication(terms)
    case Term.Lambda(parameters, body) => processLambda(parameters, body)
    case Term.IfExpression(conditional, ifBody, elseBody) => processIfExpression(conditional, ifBody, elseBody)
  }

def processApplication(terms: Seq[Term]): Either[WanderError, Expression.Application] = {
  if terms.length < 2 then
    Left(WanderError("Applications require a function name and at least one arguement."))
  else
    var error: Option[WanderError] = None
    var name: Option[Name] = None
    val itr = terms.iterator
    itr.next() match {
      case Term.NameTerm(value) => name = Some(value)
      case _ => error = Some(WanderError("Head of an Application must be a name.")) //TODO true, until I add pipes
    }
    val buffer = ListBuffer[Expression]()
    while (itr.hasNext && error.isEmpty) {
      val token = itr.next()
      process(token) match {
        case Left(value) => error = Some(value)
        case Right(value) => buffer += value
      }
    }
    if error.isEmpty then
      Right(Expression.Application(name.get, buffer.toSeq))
    else
      Left(error.get)
}

def processIfExpression(conditional: Term, ifBody: Term, elseBody: Term): Either[WanderError, Expression.IfExpression] = {
  val res = for {
    c <- process(conditional)
    i <- process(ifBody)
    e <- process(elseBody)
  } yield (c, i, e)
  res match {
    case Right((c, i, e)) => Right(Expression.IfExpression(c, i, e))
    case Left(e) => Left(e)
  }
}

def processLambda(parameters: Seq[Name], body: Term): Either[WanderError, Expression.Lambda] = {
  process(body) match {
    case Left(value) => Left(value)
    case Right(value) => Right(Expression.Lambda(parameters, value))
  }
}

def processLetExpression(decls: Seq[(Name, Term)], body: Term): Either[WanderError, Expression.LetExpression] = {
  val expressions = decls.map((name, term) => {
    process(term) match {
      case Left(value) => ???
      case Right(expression) => (name, expression)
    }
  })
  process(body) match {
    case Left(value) => ???
    case Right(body) => Right(Expression.LetExpression(expressions, body))
  }
}

def processRecord(decls: Seq[(Name, Term)]): Either[WanderError, Expression.Record] = {
  val expressions = decls.map((name, term) => {
    process(term) match {
      case Left(value) => ???
      case Right(expression) => (name, expression)
    }
  })
  Right(Expression.Record(expressions))
}

def processArray(terms: Seq[Term]): Either[WanderError, Expression.Array] = {
  val expressions = terms.map(t => {
    process(t) match {
      case Left(value) => ???
      case Right(value) => value
    }
  })
  Right(Expression.Array(expressions))
}

def processSet(terms: Seq[Term]): Either[WanderError, Expression.Set] = {
  val expressions = terms.map(t => {
    process(t) match {
      case Left(value) => ???
      case Right(value) => value
    }
  })
  Right(Expression.Set(expressions))
}
