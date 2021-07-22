package com.sa.events.domain.eventdata

import java.net.InetSocketAddress
import java.sql.SQLException

import cats.effect.{Blocker, Sync}
import com.sa.imdb.domain.meta.EventDataRepositoryAlgebra
import fs2.io.tcp.{Socket, SocketGroup}

import scala.concurrent.duration._

/**
 * EventDataService: Service layer, business logic for handling events
 * binds with Doobie EventDataRepository for making DB calls.
 *
 * This implementation uses cats Ref[F, A] which is side effect-ful for reasons:
 *  - to keep the property of referential transparency while sharing and mutating state.
 *  - when we invoke flatMap on it twice we get two different mutable states
 *  - It gives way more control than having a val ref hanging around in our code and gives local reasoning.
 *
 * Benefits because of Referential Transparency
 *  - being able to understand and build code in a compositional way.
 *  - That is, understanding code by understanding individual parts
 *    and putting them back together
 *  - And building code by building individual parts and combining them together.
 *  - This is only possible if local reasoning is guaranteed
 *  - Because otherwise there will be weird interactions when we put things back together
 *  - and referential transparency is defined as something that guarantees local reasoning.
 *
 *  Case of state sharing
 *  - since the only way to share is passing things as an argument,
 *  - the regions of sharing are exactly the same of our call graph
 *  - Instead of focusing on “who shares this state?”
 *  - we focus on “what methods take this argument?”.
 *  - This makes shared state in pure FP a lot easier to reason about than its side-effect-ful counterpart.
 */

import cats.data.{EitherT, StateT}
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.syntax.all._
import fs2.Stream

import scala.collection.mutable

class EventDataService[F[_]](eventDataRepo: EventDataRepositoryAlgebra[F])
                              (implicit F: ConcurrentEffect[F]
                               , timer: Timer[F]
                               , contextShift: ContextShift[F]){

  def execute(blocker: Blocker, inetSocketAddress: InetSocketAddress)(implicit F: ConcurrentEffect[F]
                                                                            , timer: Timer[F]
                                                                            , contextShift: ContextShift[F])
  = {
    SocketGroup[F](blocker).use { sg =>
      sg.client[F](inetSocketAddress
        , true, 256 * 1024, 256 * 1024
        , true).use(init(_).compile.drain)
    }
  }

  private def init(socket: Socket[F])(implicit F: ConcurrentEffect[F]
                                    , timer: Timer[F]
                                    , contextShift: ContextShift[F]) =
    for {
      _ <- Stream.eval(F.delay(println(s"IN INIT")))
      initState = EventCountState(mutable.Map[String,Int]())
      /*
        flatMap once and pass the reference as an argument!
        We need to call flatMap once up in the call chain where we call the processes
        to make sure they all share the same state.
     */
      ecs <- Stream.eval(Ref.of[F,EventCountState](initState))
      _ <- Stream.awakeEvery[F](10 seconds) >> tcpStream(socket,ecs)
    } yield ()

  import io.circe.parser.decode
  def tcpStream(socket: Socket[F]
                , eventCountStateRef: Ref[F,EventCountState])(implicit F: ConcurrentEffect[F]
                       , timer: Timer[F]
                       , contextShift: ContextShift[F])
                      : Stream[F,Unit] = {
    /*
      Create a Stream source: using socket read
        - read lines and decode EventData
        - ignore bad data
        - group collection by event_type
     */
    val eventDataSource = Stream.eval(socket.read(4096))
      .unNone
      .map{ ch =>
        ch.toList.map(_.toChar).mkString("")
          .split("\n").toList
          .flatMap { s =>
            val decodedEventData = decode[EventData](s)
            decodedEventData.toOption
          }
          .groupBy(_.event_type)
          .map { entry =>
            println(s"${entry._1} --- ${entry._2.size} ")
            entry._1 -> entry._2.size
          }
      }

    eventDataSource
      .through(eventProcessPipe(eventCountStateRef))
      .debug(s => s"number of event counts after pipe ${s.map}")
      .through(eventPersistPipe)
  }

  private def eventProcessPipe(eventCountStateRef: Ref[F,EventCountState])
                                            (implicit F: ConcurrentEffect[F]
                                                , timer: Timer[F]
                                                , contextShift: ContextShift[F])
      : Stream[F, Map[String,Int]] => Stream[F, EventCountState] = {
    _.evalMap { eventMap =>
      for {
        // TODO: Thread debugs for experimenting with parallel execution, to be removed
        _ <- F.delay {println(s"Stage processing Event state by ${Thread.currentThread().getName}")}
        ecs <- eventCountStateRef.get
        nextState <- execNextState(eventMap).runS(ecs)
        _ <- F.delay(println(s"ns: ${nextState.map}"))
      }yield nextState
    }
  }

  private def eventPersistPipe (implicit F: ConcurrentEffect[F]
                         , timer: Timer[F]
                         , contextShift: ContextShift[F]): Stream[F, EventCountState] => Stream[F, Unit] =
    _.evalMap { ecs =>
      import cats.syntax.flatMap._
      for {
        // TODO: Thread debugs for experimenting with parallel execution, to be removed
        _ <- F.delay {println(s"Stage  Updating DB Event state by ${Thread.currentThread().getName}")}
        r <- eventDataRepo.updateEventCountMap(ecs.map)
//        _ <- F.delay(println(s"ns: "))
      }yield ()
    }

  /*
    Mutate EventCountState
   */
  private def execNextState(ecMap: Map[String,Int])
                                 (implicit F: ConcurrentEffect[F]
                                  , timer: Timer[F]
                                  , contextShift: ContextShift[F]): StateT[F,EventCountState,Unit] = {
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

  /**
   *
   * @return    All Event types and their counts
   */
  def getCurrentEventState() = {
    for {
      ed <- eventDataRepo.getEventData()
      _ <- F.delay(println(s"ED:::::: $ed"))
    } yield ed
  }
}

object EventDataService {
  def apply[F[_]](eventDataRepo: EventDataRepositoryAlgebra[F])
                 (implicit F: ConcurrentEffect[F]
                  , timer: Timer[F]
                  , contextShift: ContextShift[F]): EventDataService[F] =
    new EventDataService[F](eventDataRepo)
}
