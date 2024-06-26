/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.inmemory

import cats.effect.IO
import dev.ligature._
import java.util.UUID
import cats.data.EitherT
import java.util.concurrent.locks.Lock

/** Represents a WriteTx within the context of a Ligature instance and a single
  * Dataset
  */
class InMemoryWriteTx(private val store: DatasetStore) extends WriteTx {
  private var _isCanceled = false
  private var _newDatasetStore = store.copy()

  /** Creates a new, unique Entity within this Dataset. */
  override def newIdentifier(prefix: String): IO[Identifier] = IO.defer {
    // TODO needs to assert that the generated Id is unique within this Dataset
    Identifier.fromString(prefix + UUID.randomUUID()) match {
      case Right(id) => IO(id)
      case Left(_) =>
        IO.raiseError(
          RuntimeException(s"Illegal Identifier Prefix - ${prefix}")
        )
    }
  }

  private def newAnonymousEntityInternal(
      prefix: String = ""
  ): Either[LigatureError, Identifier] =
    Identifier.fromString(prefix + UUID.randomUUID())

  /** Adds a given Statement to this Dataset. If the Statement already exists
    * nothing happens (TODO maybe add it with a new context?). Note: Potentially
    * could trigger a ValidationError
    */
  override def addStatement(statement: Statement): IO[Unit] = IO {
    _newDatasetStore = _newDatasetStore.copy(statements =
      _newDatasetStore.statements + statement
    )
    ()
  }

  /** Removes a given Statement from this Dataset. If the Statement doesn't
    * exist nothing happens and returns Ok(false). This function returns
    * Ok(true) only if the given Statement was found and removed. Note:
    * Potentially could trigger a ValidationError.
    */
  def removeStatement(persistedStatement: Statement): IO[Unit] = IO {
    if (_newDatasetStore.statements.contains(persistedStatement)) {
      _newDatasetStore = _newDatasetStore.copy(statements =
        _newDatasetStore.statements.excl(persistedStatement)
      )
      ()
    } else {
      ()
    }
  }

  /** Returns the DatasetStore that has been modified by this WriteTx. Used in
    * the release method of the Resource[IO, WriteTx].
    */
  private[inmemory] def newDatasetStore(): DatasetStore = _newDatasetStore
}
