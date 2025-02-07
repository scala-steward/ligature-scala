/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.wander

import dev.ligature.gaze.{Gaze, Nibbler}
import dev.ligature.gaze.Result
import dev.ligature.gaze.SeqSource
import scala.util.boundary
import scala.util.boundary.break
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer

enum Term:
  case Element(value: String)
  case Literal(value: String)
  case Network(value: Set[Triple])
  case Quote(value: Seq[Term])

def parse(script: Seq[Token]): Either[WanderError, Seq[Term]] = {
  val filteredInput = script.filter {
    _ match
      case Token.Spaces(_) | Token.NewLine | Token.Comment => false
      case _                                               => true
  }
  val gaze = Gaze(SeqSource(filteredInput))
  val res: Result[Seq[Term]] = gaze.attempt(scriptNib)
  res match {
    case Result.NoMatch =>
      if (gaze.isComplete) {
        Right(Seq())
      } else {
        Left(WanderError(s"Error Parsing - No Match - Next Token: ${gaze.next()}"))
      }
    case Result.Match(res) =>
      if (gaze.isComplete) {
        Right(res)
      } else {
        Left(WanderError(s"Error Parsing - No Match - Next Token: ${gaze.next()}"))
      }
    case Result.EmptyMatch => ??? // Right(Seq(Term.Module(Seq())))
  }
}

val literalNib: Nibbler[Token, Term.Literal] = gaze =>
  gaze.next() match
    case Some(Token.Literal(value)) => Result.Match(Term.Literal(value))
    case _                          => Result.NoMatch

val elementNib: Nibbler[Token, Term.Element] = gaze =>
  gaze.next() match
    case Some(Token.Element(value)) => Result.Match(Term.Element(value))
    case _                          => Result.NoMatch

// Handles parsing a network, assumes the initial "{"" token has already been parsed.
val partialNetworkNib: Nibbler[Token, Term.Network] = { gaze =>
  var cont = true
  val entries = ListBuffer[Triple]()
  val currentEntry = ListBuffer[LigatureValue]()
  while cont do
    gaze.next() match {
      case Some(Token.Element(token)) =>
        currentEntry.addOne(LigatureValue.Element(token))
      case Some(Token.CloseBrace) =>
        currentEntry.toList match {
          case (element: LigatureValue.Element) :: (attribute: LigatureValue.Element) :: (value: Value) :: Nil =>
            entries.addOne(element, attribute, value)
            currentEntry.clear()
          case Nil => ()
          case _ => ???
        }
        cont = false
      case Some(Token.Comma) =>
        currentEntry.toList match {
          case (element: LigatureValue.Element) :: (attribute: LigatureValue.Element) :: (value: Value) :: Nil =>
            entries.addOne(element, attribute, value)
            currentEntry.clear()
          case Nil => ()
          case _ => ???
        }
      case None => ???
      case _    => ???
    }
  Result.Match(Term.Network(entries.toSet))
}

// Handles parsing a quote, assumes the initial "("" token has already been parsed.
val partialQuoteNib: Nibbler[Token, Term.Quote] = { gaze =>
  var cont = true
  val terms = ListBuffer[Term]()
  while cont do
    gaze.next() match {
      case Some(Token.Element(element)) => terms.addOne(Term.Element(element))
      case Some(Token.CloseParen)       => cont = false
      case None                         => ???
      case _                            => ???
    }
  Result.Match(Term.Quote(terms.toSeq))
}

val applicationNib: Nibbler[Token, Term] = { gaze =>
  var result: Option[Term] = None
  var cont = true
  while cont do
    gaze.next() match {
      case Some(Token.Element(element)) =>
        result = Some(Term.Element(element))
        cont = false
      case Some(Token.Literal(literal)) =>
        result = Some(Term.Literal(literal))
      case Some(Token.Comma) => cont = false
      case Some(Token.OpenParen) =>
        partialQuoteNib(gaze) match {
          case Result.NoMatch    => ???
          case Result.EmptyMatch => ???
          case Result.Match(quote) =>
            result = Some(quote)
            cont = false
        }
      case Some(Token.OpenBrace) =>
        partialNetworkNib(gaze) match {
          case Result.NoMatch    => ???
          case Result.EmptyMatch => ???
          case Result.Match(network) =>
            result = Some(network)
            cont = false
        }
      case None => cont = false
      case _    => ???
    }
  result match {
    case None         => ???
    case Some(result) => Result.Match(result)
  }
}

val scriptNib: Nibbler[Token, Seq[Term]] = { gaze =>
  // var pipedValue: Option[Term] = None
  val results = ArrayBuffer[Term]()
  boundary:
    while !gaze.isComplete do
      gaze.attempt(applicationNib) match
        case Result.NoMatch     => break(Result.NoMatch)
        case Result.EmptyMatch  => break(Result.NoMatch)
        case Result.Match(term) => results.addOne(term)
    break(Result.Match(results.toSeq))
}
