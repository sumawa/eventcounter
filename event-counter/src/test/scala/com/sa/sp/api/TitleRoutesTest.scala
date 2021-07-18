package com.sa.sp.api

import cats._
import cats.effect._
import com.sa.events.domain.titles.Title
import com.sa.sp.db._
import com.sa.sp.FeatureBaseSpec
import io.circe.Json
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._

final class TitleRoutesTest extends FeatureBaseSpec {
  implicit def decodeTitle: EntityDecoder[IO, Title] =
    jsonOf

  implicit def encodeTitle[A[_] : Applicative]: EntityEncoder[A, Title] =
    jsonEncoderOf

  //  private val emptyRepository: Repository[IO]                              = new TestRepository[IO](Seq.empty)

  val qAvenger = "Avengers"
  val exactTrue = true
  val exactFalse = false
  val hGenre = "horror"

  feature("Testing TitleRoutes") {
    scenario(s"GET /find querying on q ${qAvenger} and exact $exactTrue must return primaryTitle : The Avengers") {
      Given(s"""Database is running """)
      val expectedStatusCode = Status.Ok

      val response: IO[Response[IO]] = (new TitleRoutes(titleS)).routes.orNotFound.run(
        Request(method = Method.GET, uri = uri"/find?q=Avengers&exact=true")
      )
      // (bytes.map(_.toChar)).mkString
      val res = response.unsafeRunSync().body.compile.toVector.unsafeRunSync
        .map(_.toChar).mkString

      import io.circe.parser.decode
      val titleListEither = decode[List[Title]](res)
      When(s"if submit request is made ")
      //          println(s"titleList $titleList")
      val isProcessed = titleListEither match {
        case Left(failure) =>
          println(s"FAILED DUE TO ${failure.getMessage}")
          false
        case Right(list) =>
          println(s"GOT LIST ")
          val avgers = list.filter(_.primaryTitle == "The Avengers")
          println(s"avgers: $avgers")
          !avgers.isEmpty
      }
      Then(s"the API /find invocation should be true: $isProcessed")
      assert(isProcessed == true)
    }

    scenario(s"GET /search/title querying on genre ${hGenre} and exact $exactTrue must return primaryTitle : The Shining") {
      Given(s"""Database is running """)
      val expectedStatusCode = Status.Ok

      val response: IO[Response[IO]] = (new TitleRoutes(titleS)).routes.orNotFound.run(
        Request(method = Method.GET, uri = uri"/search/title?genres=horror")
      )
      // (bytes.map(_.toChar)).mkString
      val res = response.unsafeRunSync().body.compile.toVector.unsafeRunSync
        .map(_.toChar).mkString

      import io.circe.parser.decode
      val titleListEither = decode[List[Title]](res)
      When(s"if submit request is made ")
      //          println(s"titleList $titleList")
      val isProcessed = titleListEither match {
        case Left(failure) =>
          println(s"FAILED DUE TO ${failure.getMessage}")
          false
        case Right(list) =>
          println(s"GOT LIST ")
          val shining = list.filter(_.primaryTitle == "The Shining")
          println(s"shining: $shining")
          !shining.isEmpty
      }
      Then(s"the API /search invocation should be true: $isProcessed")
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
