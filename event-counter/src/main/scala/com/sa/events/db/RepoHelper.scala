package com.sa.events.db

import cats.effect.{Async, ConcurrentEffect, ContextShift, IO, Sync, Timer}
import com.sa.events.config.DatabaseConfig
import cats.implicits._
import com.sa.tickets.db.DoobieEventDataRepositoryInterpreter


object RepoHelper {

  def getEventRepo[F[_]](dbConfig: DatabaseConfig)
                        (implicit F: ConcurrentEffect[F]
                         , contextShift: ContextShift[F]
                        , timer: Timer[F])
                         = {
    for {
      xa <- PooledTransactor[F](dbConfig)
      _ <- F.delay(println(s"Got XA: $xa"))
      eventRepo = DoobieEventDataRepositoryInterpreter[F](xa)
    } yield eventRepo
  }

  def bootstrap[F[_]](repo: DoobieEventDataRepositoryInterpreter[F])(implicit F: Sync[F]) = {
    for {
      _ <-  repo.createTables()
//      _ <- F.delay(println(s"created tables: ${created}"))
//      eventCounts1 = List(
//        ("foo",10)
//        , ("bar",10)
//      )
//      insertedEventCounts <- repo.insertEventCounts(eventCounts1)
//      _ <- F.delay(println(s"INSERTED EVENT COUNTS: ${insertedEventCounts} "))
//      allEventCounts <- repo.getEventData()
//      _ <- F.delay(println(s"f COUNT EVENTS: ${allEventCounts}  BOOTSTRAP PING SUCCEEDED"))
//      d1 <- repo.deleteEventCountByType("foo")
//      d2 <- repo.deleteEventCountByType("bar")
//      _ <- F.delay(println(s"Delete ${d1} ${d2} "))
//      eventCountsAfter <- repo.countEventCounts()
//      _ <- F.delay(println(s"f COUNT Events AFTER DELETE: ${eventCountsAfter}  BOOTSTRAP PING SUCCEEDED"))
    } yield ()
  }

}
