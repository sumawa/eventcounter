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
 * EventWSRoutes
 * A WebSocket endpoint for sending back updated state periodically
 * Http endpoint for querying current event counts.
 *
 * @tparam F
 * @param  eventDataService EventDataService
 */
final class EventWSRoutes[F[_]](eventDataService: EventDataService[F])
                               (implicit F: ConcurrentEffect[F]
                              , contextShift: ContextShift[F]
                               , timer: Timer[F]) extends Http4sDsl[F]{

  // bring JSON codecs in scope for http4s
  implicit def decodeTitle: EntityDecoder[F,EventData] =
    jsonOf

  implicit def encodeTitle[A[_]: Applicative]: EntityEncoder[A, EventData] =
    jsonEncoderOf

  val printer = Printer.spaces2.copy(dropNullValues = true)

  import cats.syntax.flatMap._
  import cats.syntax.functor._

  val routes: HttpRoutes[F] = HttpRoutes.of[F]{
        // get updated event type counts periodically
        // A websocket endpoint
    case GET -> Root / "eventData" / "ws1"  =>
      val toClient: Stream[F, WebSocketFrame] = {
        val resp = eventDataService.getCurrentEventState().value.flatMap {
          case Left(error) =>
            F.delay(Text(s"Error: $error"))
          case Right(v) =>
            val jsonOutput = v.asJson
            val prettyOutput = jsonOutput.printWith(printer)
//            println(s"events currently fetched: $prettyOutput")
            F.delay(Text(prettyOutput))

        }
        Stream.awakeEvery[F](10.seconds) >> Stream.eval(resp)
      }
      val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
        case Text(t, _) => F.delay(println(s"RECEIVED FROM CLIENT: $t"))
        case f =>
          F.delay(println(s"F : $f "))
      }
      WebSocketBuilder[F].build(toClient, fromClient)

      // A regular HTTP Get end point, same as the websocket above.
    case GET -> Root /"eventData"  => {
      println(s"GET -> Root / eventData ")
      eventDataService.getCurrentEventState().value.flatMap {
        case Left(error) =>
          NotFound(s"Error: $error")
        case Right(v) =>
          val jsonOutput = v.asJson
          val prettyOutput = jsonOutput.printWith(printer)
          //            println(s"events currently fetched: $prettyOutput")
          Ok(prettyOutput)
      }
    }
  }
}
