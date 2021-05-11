/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.wander.parser

import arrow.core.Either
import dev.ligature.wander.error.*
import dev.ligature.wander.lexer.WanderToken

class Parser {
    fun parse(tokens: List<WanderToken>): Either<ParserError, Script> {
        val tokenScanner = TokenScanner(tokens)
        val statements = mutableListOf<WanderStatement>()
        while (!tokenScanner.isComplete()) {
            when (val token = tokenScanner.peek()) {
                else -> Either.Left(ParserError("Unexpected token, ${token!!.debug}", token.offset))
            }
        }
        return Either.Right(Script(statements))
    }
}
