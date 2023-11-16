/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.gaze

import dev.ligature.gaze.Gaze
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** A Nibbler that takes a single item.
  */
def take[I](toMatch: I): Nibbler[I, I] = { gaze =>
  gaze.next() match {
    case Result.Match(i) =>
      if (toMatch == i) { Result.Match(toMatch) }
      else { Result.NoMatch }
    case Result.NoMatch => Result.NoMatch
    case Result.EmptyMatch => ???
  }
}

/** A Nibbler that takes a single item if the condition passed is true.
  */
def takeCond[I](cond: (I) => Boolean): Nibbler[I, I] = { gaze =>
  gaze.next() match {
    case Result.Match(i) =>
      if (cond(i)) { Result.Match(i) }
      else { Result.NoMatch }
    case Result.NoMatch => Result.NoMatch
    case Result.EmptyMatch => ???
  }
}

def takeAll[I, O](
    nibblers: Nibbler[I, O]*
): Nibbler[I, Seq[O]] = { (gaze: Gaze[I]) =>
  val results = ListBuffer[O]()
  val res = nibblers.forall { nibbler =>
    val res = gaze.attempt(nibbler)
    res match {
      case Result.Match(res) =>
        results += res
        true
      case Result.NoMatch =>
        false
      case Result.EmptyMatch => true
    }
  }
  if (res) {
    Result.Match(results.toSeq)
  } else {
    Result.NoMatch
  }
}

// def takeAllGrouped[I](
//     nibblers: Nibbler[I, I]*
// ): Nibbler[I, Seq[I]] = { (gaze: Gaze[I]) =>
//   val results = ArrayBuffer[Seq[I]]()
//   val res = nibblers.forall { nibbler =>
//     val res = gaze.attempt(nibbler)
//     res match {
//       case Some(res) =>
//         results.append(res)
//         true
//       case None =>
//         false
//     }
//   }
//   if (res) {
//     Some(results.toList)
//   } else {
//     None
//   }
// }

def takeChar(toMatch: Char): Nibbler[Char, Char] = {
  return gaze => {
    var matched = true
    val nextChar = gaze.next()
    nextChar match {
      case Result.Match(c) =>
        if (toMatch == c) {
          Result.Match(toMatch)
        } else {
          Result.NoMatch
        }
      case Result.NoMatch =>
        Result.NoMatch
      case Result.EmptyMatch => ???
    }
  }
}

def takeString(toMatch: String): Nibbler[Char, Seq[Char]] = {
  val chars = toMatch.toVector
  return gaze => {
    var offset = 0
    var matched = true
    while (matched && offset < chars.length) {
      val nextChar = gaze.next()
      nextChar match {
        case Result.Match(c) =>
          if (chars(offset) == c) {
            offset += 1;
          } else {
            matched = false
          }
        case Result.NoMatch =>
          matched = false
        case Result.EmptyMatch => ???
      }
    }
    if (matched) {
      Result.Match(chars)
    } else {
      Result.NoMatch
    }
  }
}

def takeUntil[I](toMatch: I): Nibbler[I, Seq[I]] =
  return gaze => {
    val result = ArrayBuffer[I]()
    var matched = false
    while (!matched) {
      val next = gaze.peek()
      next match {
        case Result.Match(v) =>
          if (v == toMatch) {
            matched = true
          } else {
            gaze.next()
            result.append(v)
          }
        case Result.NoMatch =>
          matched = true
        case Result.EmptyMatch => ???
      }
    }
    Result.Match(result.toSeq)
  }

//TODO needs tests
def takeUntil[I](toMatch: Nibbler[I, I]): Nibbler[I, Seq[I]] =
  return gaze => {
    val result = ArrayBuffer[I]()
    var matched = false
    while (!matched) {
      val next = gaze.peek()
      next match {
        case Result.Match(v) =>
          val check = gaze.check(toMatch)
          check match {
            case Result.Match(_) => matched = true
            case Result.NoMatch =>
              gaze.next()
              result.append(v)
            case Result.EmptyMatch => ???
          }
        case Result.NoMatch =>
          matched = true
        case Result.EmptyMatch => ???
      }
    }
    Result.Match(result.toSeq)
  }

def takeWhile[I](
    predicate: (toMatch: I) => Boolean
): Nibbler[I, Seq[I]] =
  return (gaze: Gaze[I]) => {
    val res = ArrayBuffer[I]()
    var matched = true
    var continue = true
    while (continue) {
      val peek = gaze.peek();

      peek match {
        case Result.Match(c) =>
          if (predicate(c)) {
            gaze.next();
            res += c;
          } else if (res.length == 0) {
            matched = false
            continue = false
          } else {
            continue = false
          }
        case Result.NoMatch =>
          if (res.length == 0) {
            matched = false
            continue = false
          } else {
            continue = false
          }
        case Result.EmptyMatch => ???
      }
    }
    if (matched) {
      Result.Match(res.toSeq)
    } else {
      Result.NoMatch
    }
  }

def optional[I, O](nibbler: Nibbler[I, O]): Nibbler[I, O] = { (gaze: Gaze[I]) =>
  gaze.attempt(nibbler) match {
    case res: Result.Match[_] => res
    case Result.NoMatch | Result.EmptyMatch => Result.EmptyMatch
  }
}

def takeCharacters(chars: Char*): Nibbler[Char, Seq[Char]] = takeWhile {
  chars.contains(_)
}

def takeFirst[I, O](
    nibblers: Nibbler[I, O]*
): Nibbler[I, O] = { (gaze: Gaze[I]) =>
  var finalRes: Result[O] = Result.NoMatch
  val nibbler = nibblers.find { nibbler =>
    finalRes = gaze.attempt(nibbler)
    finalRes match {
      case Result.EmptyMatch | Result.Match(_) => true
      case Result.NoMatch => false
    }
  }
  finalRes
}

def repeat[I, O](
    nibbler: Nibbler[I, O]
): Nibbler[I, Seq[O]] = { (gaze: Gaze[I]) =>
  val allMatches = ArrayBuffer[O]()
  var continue = true
  while (!gaze.isComplete && continue)
    gaze.attempt(nibbler) match {
      case Result.NoMatch    => continue = false
      case Result.Match(v) => allMatches += v
      case Result.EmptyMatch => continue = true
    }
  if (allMatches.isEmpty) {
    Result.NoMatch
  } else {
    Result.Match(allMatches.toSeq)
  }
}

def between[I, O](
    wrapper: Nibbler[I, O],
    content: Nibbler[I, O]
) = takeAll(wrapper, content, wrapper).map(_(1))

def between[I, O](
    open: Nibbler[I, O],
    content: Nibbler[I, O],
    close: Nibbler[I, O]
) = takeAll(open, content, close).map(_(1))
