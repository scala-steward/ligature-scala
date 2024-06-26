/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.gaze

import munit.FunSuite

class TakeAllGroupedSuite extends FunSuite {
  val nibbler5 = take('5')
  val nibbler6 = take('6')
  val nibbler7 = take('7')
  val nibbler8 = take('8')

  test("multiple nibblers succeed") {
    val gaze = Gaze.from("5678")
    val takeAllNib = takeAllGrouped(nibbler5, nibbler6, nibbler7, nibbler8)
    val res = gaze.attempt(takeAllNib)
    assertEquals(res, Some(List(Seq('5'), Seq('6'), Seq('7'), Seq('8'))))
    assert(gaze.isComplete())
  }

  test("multiple nibblers fail and retry") {
    val gaze = Gaze.from("5678")

    val takeAllFail = takeAllGrouped(nibbler5, nibbler6, nibbler8)
    val res = gaze.attempt(takeAllFail)
    assertEquals(res, None)
    assert(!gaze.isComplete())

    val takeAllSucceed = takeAllGrouped(nibbler5, nibbler6, nibbler7, nibbler8)
    val res2 = gaze.attempt(takeAllSucceed)
    assertEquals(res2, Some(List(Seq('5'), Seq('6'), Seq('7'), Seq('8'))))
    assert(gaze.isComplete())
  }
}
