package com.sa.events.domain.eventdata

import java.net.InetSocketAddress

import cats.effect.Blocker
import fs2.io.tcp.{Socket, SocketGroup}

import scala.concurrent.duration._

/**
 * NameServiceWithRef: Service layer, business logic for namess
 * binds with Doobie Repository for making DB calls.
 *
 * This implementation uses cats Ref
 *
Ref[F, A] is side-effect-ful, because,
      + wanting to keep the property of referential transparency while sharing and mutating state.
      + when we invoke flatMap on it twice we get two different mutable states,
      + and this is the power of Referential Transparency.
      + It gives way more control than having a val ref hanging around in our code and gives local reasoning.

    Benefits of Referential Transparency
    - being able to understand and build code in a compositional way.
    - That is, understanding code by understanding individual parts
      and putting them back together
    - And building code by building individual parts and combining them together.
    - This is only possible if local reasoning is guaranteed
    - Because otherwise there will be weird interactions when we put things back together
    - and referential transparency is defined as something that guarantees local reasoning.

    Case of state sharing
        - since the only way to share is passing things as an argument,
        - the regions of sharing are exactly the same of our call graph
        - so we transform an important aspect of the behaviour (“who shares this state?”)
        - into a straightforward syntactical property (“what methods take this argument”?).
        - This makes shared state in pure FP a lot easier to reason about than its side-effect-ful counterpart.

 */

import cats.data.{EitherT, StateT}
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.syntax.all._
import fs2.Stream

import scala.collection.mutable

object EventDataService1{

  def execute[F[_]](blocker: Blocker, inetSocketAddress: InetSocketAddress)(implicit F: ConcurrentEffect[F]
                                      , timer: Timer[F]
                                      , contextShift: ContextShift[F])
           = {
    SocketGroup[F](blocker).use { sg =>
      sg.client[F](inetSocketAddress
        , true, 256 * 1024, 256 * 1024
        , true).use(init(_).compile.drain)
    }
  }
//  import cats.implicits._

  def init[F[_]](socket: Socket[F])(implicit F: ConcurrentEffect[F]
                                    , timer: Timer[F]
                                    , contextShift: ContextShift[F]) =
    for {
    serverAddr <- Stream.eval(
      F.delay(new InetSocketAddress("localhost", 9999))
    )
    _ <- Stream.eval(F.delay(println(s"IN INIT")))
    initState = EventCountState(mutable.Map[String,Int]())
    ecs <- Stream.eval(Ref.of[F,EventCountState](initState))
    res <- Stream.awakeEvery[F](10 seconds) >> tcpStream(socket,ecs)
    //    res <- Stream.awakeEvery[F](10 seconds) >> tcpStream(socket)
  } yield ()

  /*
  flatMap once and pass the reference as an argument!
  We need to call flatMap once up in the call chain where we call the processes
  to make sure they all share the same state.
 */
//  import cats.implicits._
  import io.circe.parser.decode
  def tcpStream[F[_]](socket: Socket[F]
                       //                       , ecs: EventCountState
                       , eventCountStateRef: Ref[F,EventCountState]
                      )(implicit F: ConcurrentEffect[F]
                        , timer: Timer[F]
                        , contextShift: ContextShift[F])
//            : Stream[F,Unit]
    : Stream[F,EventCountState]
   = {
    /*
      Create a source: Based on current item in the queue.
      Initially queue will have only source item (Kevin Bacon)
      Subsequently, if downstream continue to request, based on other actors added in the queue,
      the new "DegreeState" is computed
     */
      val eventDataSource = Stream.eval(socket.read(4096))
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

    val finalDegreeState = eventDataSource
      .through(pipeRef(eventCountStateRef))
//      .compile
//      .last
//
//    val res = EitherT.right[String](
//      for {
//        ds <- finalDegreeState
//      } yield (ds.map(_.map))
//    )
//
//    println(s"RES: $res")
//    Stream.eval(finalDegreeState)
//    Stream.eval(F.delay(println(s" --- ")))
    finalDegreeState
  }

  private def pipeRef[F[_]](eventCountStateRef: Ref[F,EventCountState])(implicit F: ConcurrentEffect[F]
                                                                  , timer: Timer[F]
                                                                  , contextShift: ContextShift[F])
    : Stream[F, Map[String,Int]] => Stream[F, EventCountState] = {
    _.evalMap { eventMap =>
      for {
        _ <- F.delay {println(s"Stage  processing Event state by ${Thread.currentThread().getName}")}
        ecs <- eventCountStateRef.get
        nextState <- execNextState(eventMap)
          .runS(ecs)
        _ <- F.delay(println(s"ns: ${nextState.map}"))
      }yield nextState
    }
  }

  private def execNextState[F[_]](ecMap: Map[String,Int])
                           (implicit F: ConcurrentEffect[F]
                            , timer: Timer[F]
                            , contextShift: ContextShift[F])
  : StateT[F,EventCountState,Unit] = {
    for {
      currEventCountState <- StateT.get[F, EventCountState]
      _ <- StateT.set {
        val currMap = currEventCountState.map
        val c = ecMap.map{ entry =>
          val (k,v) = (entry._1,entry._2)
          if (currMap.contains(k)) {
            val currVal = currMap(k)
            println(s"currVal: $currVal")
            currMap(k) = currVal + v
          }
          else currMap(k) = v
        }
        currEventCountState
      }
    } yield ()
  }
}






