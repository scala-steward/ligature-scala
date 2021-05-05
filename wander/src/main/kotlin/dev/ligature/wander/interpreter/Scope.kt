/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.wander.interpreter

import arrow.core.Either
import dev.ligature.wander.parser.Primitive
import dev.ligature.wander.error.SymbolExits
import dev.ligature.wander.error.UnknownSymbol

/**
 * A Scope represents the values stored in the current scope, but can also reference parent scopes.
 */
class Scope(private val parentScope: Scope?) {
    private val symbols = mutableMapOf<String, Primitive>()

    fun addSymbol(name: String, value: Primitive): Either<SymbolExits, Unit> =
        synchronized(this) {
            if (symbols.containsKey(name)) {
                Either.Left(SymbolExits(name))
            } else {
                symbols[name] = value
                Either.Right(Unit)
            }
        }

    fun lookupSymbol(name: String): Either<UnknownSymbol, Primitive> =
        synchronized(this) {
            if (symbols.containsKey(name)) {
                Either.Right(symbols[name]!!)
            } else {
                parentScope?.lookupSymbol(name) ?: Either.Left(UnknownSymbol(name))
            }
        }
}