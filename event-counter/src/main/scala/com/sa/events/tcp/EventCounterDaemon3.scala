package com.sa.events.tcp

import cats.effect.concurrent.Ref
import cats.effect.{Blocker, Bracket, ConcurrentEffect, ContextShift, Timer}
import com.sa.events.domain.eventdata.{EventCountState, EventData}
import fs2.io.tcp.Socket
import io.circe.parser.decode

/*
 - java.nio.channels and java.nio.channels.Selector libraries.
 - channels represent connections to entities that
  - are capable of performing I/O operations, such as files and sockets
  - defines selectors, for multiplexed, non-blocking I/O operations.
 - selector may be created by invoking the open method of this class,
    which will use the system’s default selector provider to create a new selector.

  java.nio
    - defines buffers which are containers for the data
    - Charsets and their encoders and decoders
    - Channels which represents connection to entities capable of I/O
    - Selectors and selector keys, which together with channels defines
      multiplexed non-blocking I/O facility

  - Create CrunchifyNIOClient.java which tries to connect to server on port 1111
  - Create ArrayList with 5 company names
  - Iterate through ArrayList and send each companyName to server
  - Close connection after task finish
 */
object EventCounterDaemon3 {

  import fs2._
  import fs2.io.tcp.SocketGroup

  import scala.concurrent.duration._

  // https://github.com/typelevel/fs2/issues/1300


  import java.net.InetSocketAddress

  def tcpStream[F[_]](socket: Socket[F])
                     (implicit F: ConcurrentEffect[F]
                      , timer: Timer[F]
                      , contextShift: ContextShift[F]): Stream[F, Option[Map[String, Int]]] = {

    val outcome = for {
      chunks <- Stream.eval {
        println(s"READING CHUNKS")
        socket.read(4096)
      }
      strList <- Stream.eval {
        F.delay(chunks.map(_.toList.map(_.toChar).mkString("")))
      }
    } yield {
      val r = strList.map { str =>
        str.split("\n").toList
          .flatMap { s =>
            val decodedEventData = decode[EventData](s)
            decodedEventData.toOption
          }
      }
      println(s" GOT EVENT DATA SIZE ${r.map(_.size)}")
      //      println(s"R: $r")
      val stringListMapOpt = r.map { p => p.groupBy(_.event_type) }
      val wc = stringListMapOpt.map(_.map(entry => entry._1 -> entry._2.size))
      println(s"WC Map: $wc")
      wc
    }
    outcome
  }

  def execute[F[_]](blocker: Blocker)
                   (implicit F: ConcurrentEffect[F]
                    , timer: Timer[F]
                    , contextShift: ContextShift[F]) = {
    SocketGroup[F](blocker).use { sg =>
      sg.client[F](new InetSocketAddress("localhost", 9999)
        , true, 256 * 1024, 256 * 1024
        , true).use(init(_).compile.drain)
    }
  }

  def init[F[_]](socket: Socket[F])
                (implicit F: ConcurrentEffect[F]
                 , timer: Timer[F]
                 , cs: ContextShift[F]) = for {
    serverAddr <- Stream.eval(
      F.delay(new InetSocketAddress("localhost", 9999))
    )
    _ <- Stream.eval(F.delay(println(s"IN INIT")))
    res <- Stream.awakeEvery[F](10 seconds) >> tcpStream1(socket)
//    res <- Stream.awakeEvery[F](10 seconds) >> tcpStream(socket)
  } yield ()

  /*
    flatMap once and pass the reference as an argument!
    We need to call flatMap once up in the call chain where we call the processes
    to make sure they all share the same state.
   */
  def tcpStream1[F[_]](socket: Socket[F]
                       //                       , ecs: EventCountState
//                       , eventCountStateRef: Ref[F,EventCountState]
                      )
                      (implicit F: ConcurrentEffect[F]
                       , timer: Timer[F]
                       , contextShift: ContextShift[F])
  //            : Stream[F, Option[Map[String, Int]]] = {
              : Stream[F,Unit] = {
//  : Stream[F,EventCountState] = {

    val eventData = Stream.eval(socket.read(4096))
      .unNone
      .map{ ch =>
        //        println(s"getting chunks")
        ch.toList.map(_.toChar).mkString("")
          .split("\n").toList
          .flatMap { s =>
//            println(s"decoding events")
            val decodedEventData = decode[EventData](s)
            decodedEventData.toOption
          }
          .groupBy(_.event_type)
          .map { entry =>
            println(s"${entry._1} --- ${entry._2.size} ")
            entry._1 -> entry._2.size
          }
      }
//      .map{ e =>
//        println(s"updating state: ${e.size}")
//        e.map{ ee =>
//          updateEventRef(ee._1,ee._2,eventCountStateRef)
//          println(s"eventcount: ${eventCountStateRef.get}")
//        }}
      .compile.drain
    //    eventData
    //    println(s"eventData: ${eventCountStateRef.get}")
//    Stream.eval(eventCountStateRef.get)
        Stream.eval(eventData)
  }

  import cats.implicits._
  import cats.data.StateT
  def updateEventRef[F[_]](s: String, i: Int, eventCountStateRef: Ref[F,EventCountState])
                          (implicit F: ConcurrentEffect[F]
                           , timer: Timer[F]
                           , contextShift: ContextShift[F])
  = {
    for{
      _ <- F.delay(println(s"TRIGGERING UPDATE"))
      ecs <- eventCountStateRef.get
      _ <- F.delay{
        val v = ecs.map(s)
        ecs.map(s) = v + i
      }
      _ <- F.delay(println(s"ECSSSS $ecs"))
    }yield ()
  }

}