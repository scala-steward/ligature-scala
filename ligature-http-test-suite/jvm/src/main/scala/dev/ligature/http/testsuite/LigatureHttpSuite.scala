/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

package dev.ligature.http.testsuite

import dev.ligature.{Dataset, Statement}
import dev.ligature.lig.read
import dev.ligature.http.LigatureHttp
import cats.effect.IO
import fs2.Stream
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import munit.*
import org.http4s.*
import org.http4s.client.dsl.io.*
import org.http4s.dsl.io.*
import org.http4s.syntax.all.*
import cats.effect.kernel.Resource
import dev.ligature.Ligature
import dev.ligature.http.AuthMode
import com.comcast.ip4s.Port

abstract class LigatureHttpSuite extends CatsEffectSuite {
  def createLigature(): Resource[IO, Ligature]

  def createInstance() =
    createLigature().use { instance =>
      IO.pure(LigatureHttp(instance, AuthMode.None, Port.fromInt(4202).get))
    }

  // test("Datasets should initially be empty") {
  //   val res = for {
  //     instance <- createInstance()
  //     response <- instance.routes.run(Request(method = Method.GET, uri = uri"/datasets"))
  //     res <- response.bodyText.compile.string
  //   } yield res
  //   assertIO(res, "[]")
  // }

  // test("Add Datasets") {
  //   val instance = createInstance()
  //   instance.routes
  //     .run(Request(method = Method.POST, uri = uri"/datasets/new"))
  //     .unsafeRunSync()
  //   val response = instance.routes
  //     .run(Request(method = Method.GET, uri = uri"/datasets"))
  //     .unsafeRunSync()
  //   val res = response.bodyText.compile.string.unsafeRunSync()
  //   assertEquals(res, "[\"new\"]")
  // }

  // test("Query Datasets w/ prefix") {
  //   val writes = List("test/test1", "test/test2", "test3/test")
  //     .map { ds => toIO(() => client.post(port, local, s"/$ds").send()) }
  //     .reduce(_ >> _)

  //   val res = runServer(port, createLigature()) { _ =>
  //     for {
  //       _ <- writes
  //       res <- toIO(() => client.get(port, local, "/?prefix=test%2F").send())
  //     } yield res
  //   }.unsafeRunSync()
  //   assertEquals(
  //     JsonParser.parseString(res.bodyAsString()).getAsJsonArray,
  //     JsonParser.parseString("[\"test/test1\",\"test/test2\"]").getAsJsonArray
  //   )
  // }

  // test("Query Datasets w/ range") {
  //   val writes = List("test", "test1/test1", "test2/test2", "test3/test")
  //     .map { ds => toIO(() => client.post(port, local, s"/$ds").send()) }
  //     .reduce(_ >> _)
  //   val res = runServer(port, createLigature()) { _ =>
  //     for {
  //       _ <- writes
  //       res <- toIO(() =>
  //         client.get(port, local, "/?start=test1&end=test3").send()
  //       )
  //     } yield res
  //   }.unsafeRunSync()
  //   assertEquals(
  //     JsonParser.parseString(res.bodyAsString()).getAsJsonArray(),
  //     JsonParser
  //       .parseString("[\"test1/test1\",\"test2/test2\"]")
  //       .getAsJsonArray()
  //   )
  // }

//   test("Delete Datasets") {
//     val instance = createInstance()
//     instance.routes
//       .run(Request(method = Method.POST, uri = uri"/datasets/new2"))
//       .unsafeRunSync()
//     instance.routes
//       .run(Request(method = Method.POST, uri = uri"/datasets/new3"))
//       .unsafeRunSync()
//     instance.routes
//       .run(Request(method = Method.DELETE, uri = uri"/datasets/new3"))
//       .unsafeRunSync()
//     val response = instance.routes
//       .run(Request(method = Method.GET, uri = uri"/datasets"))
//       .unsafeRunSync()
//     val res = response.bodyText.compile.string.unsafeRunSync()
//     assertEquals(res, "[\"new2\"]")
//   }

//   test("Statements in new Dataset should start empty") {
//     val instance = createInstance()
//     instance.routes
//       .run(Request(method = Method.POST, uri = uri"/datasets/new"))
//       .unsafeRunSync()
//     val response = instance.routes
//       .run(Request(method = Method.GET, uri = uri"/datasets/new/statements"))
//       .unsafeRunSync()
//     val res = response.bodyText.compile.string.unsafeRunSync()
//     assertEquals(res, "")
//   }

//   test("Add a single Statement") {
//     val instance = createInstance()
//     instance.routes
//       .run(Request(method = Method.POST, uri = uri"/datasets/new"))
//       .unsafeRunSync()
//     val writeResponse = instance.routes
//       .run(
//         Request(method = Method.POST, uri = uri"/datasets/new/statements")
//           .withEntity("<a> <b> <c>")
//       )
//       .unsafeRunSync()
//       .bodyText
//       .compile
//       .string
//       .unsafeRunSync()
// //    assertEquals(writeResponse, "")
//     val response = instance.routes
//       .run(Request(method = Method.GET, uri = uri"/datasets/new/statements"))
//       .unsafeRunSync()
//     val res = response.bodyText.compile.string.unsafeRunSync()
//     assertEquals(res, "<a> <b> <c>\n")
//   }

//   test("Add multiple Statements") {
//     val instance = createInstance()
//     val statements =
//       """<1> <attribute> <2>
//         |<3> <attribute> <1>""".stripMargin
//     instance.routes
//       .run(Request(method = Method.POST, uri = uri"/datasets/new"))
//       .unsafeRunSync()
//     val writeResponse = instance.routes
//       .run(
//         Request(method = Method.POST, uri = uri"/datasets/new/statements")
//           .withEntity(statements)
//       )
//       .unsafeRunSync()
//       .bodyText
//       .compile
//       .string
//       .unsafeRunSync()
//     assertEquals(writeResponse, "")
//     val response = instance.routes
//       .run(Request(method = Method.GET, uri = uri"/datasets/new/statements"))
//       .unsafeRunSync()
//     val res = response.bodyText.compile.string.unsafeRunSync()
//     assertEquals(ligToSet(res), ligToSet(statements))
//   }

//   test("Delete Statements") {
//     val instance = createInstance()
//     val addStatements =
//       """
//         |<1> <attribute> <2>
//         |<3> <attribute> <1>
//         |<4> <attribute2> "Hello"
//         |<5> <attribute3> 3453
//         |<1> <attribute3> <1>
//         |""".stripMargin

//     val deleteStatements = // includes a dupe and a statement that doesn't exist
//       """
//         |<1> <attribute> <2>
//         |<6> <attribute3> 3453
//         |<1> <attribute3> <1>
//         |<1> <attribute> <2>
//         |""".stripMargin

//     val resultStatements =
//       """
//         |<3> <attribute> <1>
//         |<4> <attribute2> "Hello"
//         |<5> <attribute3> 3453
//         |""".stripMargin

//     instance.routes
//       .run(Request(method = Method.POST, uri = uri"/datasets/new"))
//       .unsafeRunSync()

//     val writeResponse = instance.routes
//       .run(
//         Request(method = Method.POST, uri = uri"/datasets/new/statements")
//           .withEntity(addStatements)
//       )
//       .unsafeRunSync()
//       .bodyText
//       .compile
//       .string
//       .unsafeRunSync()
// //    assertEquals(writeResponse, "")

//     val deleteResponse = instance.routes
//       .run(
//         Request(method = Method.DELETE, uri = uri"/datasets/new/statements")
//           .withEntity(deleteStatements)
//       )
//       .unsafeRunSync()
//       .bodyText
//       .compile
//       .string
//       .unsafeRunSync()
// //    assertEquals(deleteResponse, "")

//     val response = instance.routes
//       .run(Request(method = Method.GET, uri = uri"/datasets/new/statements"))
//       .unsafeRunSync()
//     val res = response.bodyText.compile.string.unsafeRunSync()

//     assertEquals(ligToSet(res), ligToSet(resultStatements))
//   }

  //  test("Run Wander") {
  //    val instance = createInstance()
  //    instance.routes
  //      .run(Request(method = Method.POST, uri = uri"/datasets/new"))
  //      .unsafeRunSync()
  //    val writeResponse = instance.routes
  //      .run(
  //        Request(method = Method.POST, uri = uri"/datasets/new/wander")
  //          .withEntity("and(true true)")
  //      )
  //      .unsafeRunSync()
  //      .bodyText
  //      .compile
  //      .string
  //      .unsafeRunSync()
  //    assertEquals(writeResponse, "true")
  //  }
  // }

  def ligToSet(input: String): Set[Statement] =
    read(input).getOrElse(???).toSet
}
