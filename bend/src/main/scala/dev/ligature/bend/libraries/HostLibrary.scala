/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.bend.libraries

import dev.ligature.bend.Environment
import dev.ligature.bend.WanderError
import dev.ligature.bend.BendValue
import dev.ligature.bend.FieldPath
import dev.ligature.bend.HostFunction

final class HostLibrary(hostModules: Map[ModuleId, BendValue.Module]) extends ModuleLibrary {
  override def lookup(id: ModuleId): Either[WanderError, Option[BendValue.Module]] =
    hostModules.get(id) match
      case Some(value) => Right(Some(value))
      case None        => Right(None)
}
