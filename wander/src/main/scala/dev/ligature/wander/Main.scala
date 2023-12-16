/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */


import dev.ligature.wander.preludes.common

@main
def main(args: String*) =
  val script = args.mkString(" ")
  val intro = introspect(script)
  println("\n")
  println("Script     : " + script)
  println("Tokens     : " + intro.tokens)
  println("Terms      : " + intro.terms)
  println("Expression : " + intro.expression)
  println("\n")