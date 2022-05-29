/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.wander

import dev.ligature.wander.parser.Script

case class TypeError(message: String)
object Ok

def typeCheck(script: Script): Either[List[TypeError], Ok] = {
  ???
}
