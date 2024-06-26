/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.gaze

import munit.FunSuite

import scala.collection.mutable.ArrayBuffer

private val fiveStep = takeString("5")
private val helloStep = takeString("hello")
private val spaceStep = takeString(" ")
private val worldStep = takeString("world")

class TakeStringSuite extends FunSuite {
  test("empty input") {
    val gaze = Gaze.from("")
    assertEquals(gaze.attempt(fiveStep), None)
    assert(gaze.isComplete())
  }

  test("single 5 input") {
    val gaze = Gaze.from("5")
    assertEquals(gaze.attempt(fiveStep), Some(Seq('5')))
    assert(gaze.isComplete())
  }

  test("single 4 input") {
    val gaze = Gaze.from("4")
    assertEquals(gaze.attempt(fiveStep), None)
    assert(!gaze.isComplete())
  }

  test("multiple 5s input") {
    val gaze = Gaze.from("55555")
    val res = ArrayBuffer[Char]()
    while (!gaze.isComplete()) {
      val nres = gaze.attempt(fiveStep)
      nres match {
        case Some(m) => res.appendAll(m)
        case None    => throw new Error("Should not happen")
      }
    }
    assertEquals(res.toList, List('5', '5', '5', '5', '5'))
  }

  test("hello world test") {
    val gaze = Gaze.from("hello world")
    assertEquals(gaze.attempt(helloStep), Some("hello".toSeq))
    assertEquals(gaze.attempt(spaceStep), Some(" ".toSeq))
    assertEquals(gaze.attempt(worldStep), Some("world".toSeq))
    assert(gaze.isComplete())
  }

  test("map test") {
    val gaze = Gaze.from("1")
    val oneDigit = takeString("1").map(_.map(_.asDigit))
    assertEquals(gaze.attempt(oneDigit), Some(Seq(1)))
  }
}
