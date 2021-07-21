package com.sa.events.api

import cats.Applicative
import cats.effect._
import com.sa.events.domain.eventdata.{EventData, EventDataService}
import io.circe.Printer
import io.circe.syntax._
import fs2._
import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._

import scala.concurrent.duration._

/**
 * NameWSRoutes
 * WebSocket endpoints for serving compute intensive queries
 * related to names
 *
 * @tparam F
 */
final class EventWSRoutes[F[_]](eventDataService: EventDataService[F])
                               (implicit F: ConcurrentEffect[F]
                              , contextShift: ContextShift[F]
                               , timer: Timer[F]) extends Http4sDsl[F]{

  object showId extends QueryParamDecoderMatcher[String]("uuid")
  object t extends QueryParamDecoderMatcher[String]("title")

  // bring JSON codecs in scope for http4s
  implicit def decodeTitle: EntityDecoder[F,EventData] =
    jsonOf

  implicit def encodeTitle[A[_]: Applicative]: EntityEncoder[A, EventData] =
    jsonEncoderOf

  val printer = Printer.spaces2.copy(dropNullValues = true)

  import cats.syntax.flatMap._
  import cats.syntax.functor._

  val routes: HttpRoutes[F] = HttpRoutes.of[F]{
        // get degree of separation of target actor from Kevin Bacon
    case GET -> Root / "eventData" / "ws1" / target =>
      val toClient: Stream[F, WebSocketFrame] =
//      {
//        val resp = for{
//          events <- eventDataService.getCurrentEventState()
//          _ <- F.delay(println(s"events currently fetched: $events"))
//        } yield {
//          val jsonOutput = events.asJson
//          val prettyOutput = jsonOutput.printWith(printer)
//          Text(prettyOutput)
//        }
//        Stream.eval(resp)
//      }
      {
        val resp = for {
          events <- eventDataService.getCurrentEventState()
          _ <- F.delay(println(s"events currently fetched: $events"))
        } yield {
          val jsonOutput = events.asJson
          val prettyOutput = jsonOutput.printWith(printer)
          Text(prettyOutput)
        }
        Stream.awakeEvery[F](10.seconds) >> Stream.eval(resp)
      }
//      Stream.awakeEvery[F](10.seconds).map{d =>
////        println(s"PINGING: ${d}")
////        Text(s"Ping! $d")
//      }
      val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
        case Text(t, _) => F.delay(println(t))
        case f =>
          F.delay(println(s"GOT target nconst: $f  $target"))
      }
      WebSocketBuilder[F].build(toClient, fromClient)
  }
}
