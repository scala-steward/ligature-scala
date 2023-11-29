/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.inmemory

import cats.effect.IO
import dev.ligature.*
import fs2.Stream

/** Represents a QueryTx within the context of a Ligature instance and a single
  * Dataset
  */
class InMemoryQueryTx(private val store: DatasetStore) extends QueryTx {

  /** Returns all PersistedEdges in this Dataset. */
  def allEdges(): Stream[IO, Edge] =
    Stream.emits(store.edges.toSeq)

  /** Returns all PersistedEdges that match the given criteria. If a
    * parameter is None then it matches all, so passing all Nones is the same as
    * calling allEdges.
    */
  override def matchEdges(
      source: Option[Label],
      label: Option[Label],
      target: Option[Value]
  ): Stream[IO, Edge] = {
    var res = Stream.emits(store.edges.toSeq)
    if (source.isDefined) {
      res = res.filter(_.source == source.get)
    }
    if (label.isDefined) {
      res = res.filter(_.label == label.get)
    }
    if (target.isDefined) {
      res = res.filter(_.target == target.get)
    }
    res
  }

//  /** Returns all PersistedEdges that match the given criteria. If a
//    * parameter is None then it matches all.
//    */
//  override def matchEdgesRange(
//      source: Option[Identifier],
//      label: Option[Identifier],
//      range: dev.ligature.Range
//  ): Stream[IO, Edge] = {
//    var res = Stream.emits(store.edges.toSeq)
//    if (source.isDefined) {
//      res = res.filter(_.source == source.get)
//    }
//    if (label.isDefined) {
//      res = res.filter(_.label == label.get)
//    }
//    res = res.filter { ps =>
//      val testValue = ps.target
//      (testValue, range) match {
//        case (StringLiteral(v), StringLiteralRange(start, end)) =>
//          v >= start && v < end
//        case (IntegerLiteral(v), IntegerLiteralRange(start, end)) =>
//          v >= start && v < end
//        case _ => false
//      }
//    }
//    res
//  }
}
