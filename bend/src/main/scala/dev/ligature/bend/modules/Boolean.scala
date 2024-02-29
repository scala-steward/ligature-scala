/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package dev.ligature.bend.modules

import dev.ligature.bend.Environment
import dev.ligature.bend.Token
import dev.ligature.bend.BendValue
import dev.ligature.bend.Term
import dev.ligature.bend.BendError
import dev.ligature.bend.HostFunction
import dev.ligature.bend.TaggedField
import dev.ligature.bend.Tag
import dev.ligature.bend.Field
import dev.ligature.bend.FieldPath
import dev.ligature.bend.eval
import jetbrains.exodus.bindings.BooleanBinding
import jetbrains.exodus.ArrayByteIterable

val boolModule: BendValue.Module = BendValue.Module(
  Map(
    Field("not") -> BendValue.Function(
      HostFunction(
        // FieldPath(Seq(Field("Bool"), Field("not"))),
        "Perform a not operation on a Bool value.",
        Seq(TaggedField(Field("value"), Tag.Untagged)),
        Tag.Untagged,
        (args, environment) =>
          args match
            case Seq(BendValue.Bool(value)) => Right((BendValue.Bool(!value), environment))
            case _                          => Left(BendError("Unexpected input " + args))
      )
    ),
    Field("and") -> BendValue.Function(
      HostFunction(
        // FieldPath(Seq(Field("Bool"), Field("and"))),
        "Perform a logical and on two Bools.",
        Seq(TaggedField(Field("left"), Tag.Untagged), TaggedField(Field("right"), Tag.Untagged)),
        Tag.Untagged,
        (args, environment) =>
          args match
            case Seq(BendValue.Bool(left), BendValue.Bool(right)) =>
              Right((BendValue.Bool(left && right), environment))
            case _ => ???
      )
    ),
    Field("or") -> BendValue.Function(
      HostFunction(
        // FieldPath(Seq(Field("Bool"), Field("or"))),
        "Perform a logical or on two Bools.",
        Seq(TaggedField(Field("left"), Tag.Untagged), TaggedField(Field("right"), Tag.Untagged)),
        Tag.Untagged,
        (args, environment) => ???
      )
    ),
    Field("toBytes") -> BendValue.Function(
      HostFunction(
        // FieldPath(Seq(Field("Bool"), Field("toBytes"))),
        "Encod a Bool as Bytes.",
        Seq(
          TaggedField(Field("value"), Tag.Untagged)
        ),
        Tag.Untagged,
        (args, environment) =>
          args match
            case Seq(BendValue.Bool(value)) =>
              Right(
                (
                  BendValue.Bytes(BooleanBinding.booleanToEntry(value).getBytesUnsafe().toSeq),
                  environment
                )
              )
      )
    ),
    Field("fromBytes") -> BendValue.Function(
      HostFunction(
        // FieldPath(Seq(Field("Bool"), Field("fromBytes"))),
        "Decode Bytes to a Bool.",
        Seq(
          TaggedField(Field("value"), Tag.Untagged)
        ),
        Tag.Untagged,
        (args, environment) =>
          args match
            case Seq(BendValue.Bytes(value)) =>
              Right(
                (
                  BendValue.Bool(BooleanBinding.entryToBoolean(ArrayByteIterable(value.toArray))),
                  environment
                )
              )
      )
    )
  )
)

//   stdLib = stdLib
//     .bindVariable(
//       Name("and"),
//       BendValue.HostFunction(
//         (arguments: Seq[Expression], environment: Environment) => ???
//           // if arguments.length == 2 then
//           //   val res = for {
//           //     left <- evalTerm(arguments(0), environment)
//           //     right <- evalTerm(arguments(1), environment)
//           //   } yield (left, right)
//           //   res.map { r =>
//           //     (r._1.result, r._2.result) match
//           //       case (BendValue.BooleanValue(left), BendValue.BooleanValue(right)) => BendValue.BooleanValue(left && right)
//           //       case _ => throw LigatureError("`and` function requires two booleans")
//           //   }
//           // else
//           //   IO.raiseError(LigatureError("`and` function requires two booleans"))
//       )
//     )
//     .getOrElse(???)

//   stdLib = stdLib
//     .bindVariable(
//       Name("or"),
//       BendValue.HostFunction(
//         (arguments: Seq[Expression], environment: Environment) => ???
//           // if arguments.length == 2 then
//           //   val res = for {
//           //     left <- evalTerm(arguments(0), environment)
//           //     right <- evalTerm(arguments(1), environment)
//           //   } yield (left, right)
//           //   res.map { r =>
//           //     (r._1.result, r._2.result) match
//           //       case (BendValue.BooleanValue(left), BendValue.BooleanValue(right)) => BendValue.BooleanValue(left || right)
//           //       case _ => throw LigatureError("`or` function requires two booleans")
//           //   }
//           // else
//           //   Left(BendError("`or` function requires two booleans")))
//       )
//     )
//     .getOrElse(???)