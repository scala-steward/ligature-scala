/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.gaze

import munit.FunSuite

class BetweenSuite extends FunSuite {
  val quote = takeString("'")
  val open = takeString("<")
  val close = takeString(">")
  val content = takeString("hello")

  test("quote wrap test") {
    val gaze = Gaze.from("'hello'")
    assertEquals(gaze.attempt(between(quote, content)), Some("hello".toSeq))
  }

  test("angle bracket test") {
    val gaze = Gaze.from("<hello>")
    assertEquals(
      gaze.attempt(between(open, content, close)),
      Some("hello".toSeq)
    )
  }
}
