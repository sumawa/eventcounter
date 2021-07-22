package com.sa.events.api

import cats._
import cats.effect._
import com.sa.events.domain.eventdata.EventData
import com.sa.events.db._
import com.sa.events.FeatureBaseSpec
import io.circe.Json
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._

/**
 * TODO: Dummy tests, to be filled
 */
final class EventRoutesTest extends FeatureBaseSpec {
  implicit def decodeTitle: EntityDecoder[IO, EventData] =
    jsonOf

  implicit def encodeTitle[A[_] : Applicative]: EntityEncoder[A, EventData] =
    jsonEncoderOf

  //  private val emptyRepository: EventDataRepository[IO]                              = new TestRepository[IO](Seq.empty)

  feature("Testing EventRoute WS") {
    scenario(s"GET  / eventData / ws must return This is a WebSocket route ") {
      Given(s"""Database is running """)
      println(s"GET  / eventData / ws")
      val expectedStatusCode = Status.Ok

      val response: IO[Response[IO]] = (new EventWSRoutes[IO](eventDataService)).routes.orNotFound.run(
        Request(method = Method.GET, uri = uri"/eventData/ws")
      )

      val res = response.unsafeRunSync().body.compile.toVector.unsafeRunSync
        .map(_.toChar).mkString

      println(s"RES: $res")
      assert("This is a WebSocket route".equalsIgnoreCase(res))
    }

    scenario(s"GET /eventData must return event counts") {
      Given(s"""Database is running """)
      val expectedStatusCode = Status.Ok

      val response: IO[Response[IO]] = (new EventWSRoutes[IO](eventDataService)).routes.orNotFound.run(
        Request(method = Method.GET, uri = uri"/eventData")
      )
      // (bytes.map(_.toChar)).mkString
      val res = response.unsafeRunSync().body.compile.toVector.unsafeRunSync
        .map(_.toChar).mkString

      val isProcessed = true
      assert(isProcessed == true)
    }
  }

  // helper function for automatically unwrapping results
  // out of response.
  // Return true if match succeeds; otherwise false
  def check[A](actual: IO[Response[IO]],
               expectedStatus: Status,
               expectedBody: Option[A])(
                implicit ev: EntityDecoder[IO, A]
              ): Boolean = {
    val actualResp = actual.unsafeRunSync
    println(s"actualResp: ${actualResp}")
    val statusCheck = actualResp.status == expectedStatus
    println(s"statusCheck: ${statusCheck}")
    val bodyCheck = expectedBody
      .fold[Boolean](
        actualResp.body.compile.toVector.unsafeRunSync.isEmpty)( // Verify Response's body is empty.
        expected => {
          val resp = actualResp.as[A].unsafeRunSync
          println(s"resp: ${resp}")
          resp === expected
        }
      )
    statusCheck && bodyCheck
  }
}
