package com.sa.events.db

import java.util.UUID

import cats.effect.{Async, ConcurrentEffect, ContextShift, Sync, Timer}
import com.sa.events.config.DatabaseConfig
import doobie.util.transactor.Transactor
import cats.implicits._
import cats.data.NonEmptySet
import com.sa.events.domain.eventdata.EventCount
import com.sa.tickets.db.DoobieEventDataRepositoryInterpreter


object RepoHelper {

  def getEventRepo[F[_]: ContextShift: ConcurrentEffect: Timer](dbConfig: DatabaseConfig) = {
    val tx = Transactor
      .fromDriverManager[F](dbConfig.driver, dbConfig.jdbcUrl, dbConfig.user, dbConfig.pass)
    new DoobieEventDataRepositoryInterpreter(tx)
  }

  def bootstrap[F[_]](repo: DoobieEventDataRepositoryInterpreter[F])(implicit F: Sync[F]) = {
    for {
      created <- repo.createTables()
      _ <- F.delay(println(s"created tables: ${created}"))
      eventCounts1 = List(
        ("foo",10)
        , ("bar",10)
      )
      insertedEventCounts <- repo.insertEventCounts(eventCounts1)
      _ <- F.delay(println(s"INSERTED EVENT COUNTS: ${insertedEventCounts} "))
      allEventCounts <- repo.getEventData()
      _ <- F.delay(println(s"f COUNT EVENTS: ${allEventCounts}  BOOTSTRAP PING SUCCEEDED"))
      d1 <- repo.deleteEventCountByType("foo")
      d2 <- repo.deleteEventCountByType("bar")
      _ <- F.delay(println(s"Delete ${d1} ${d2} "))
      eventCountsAfter <- repo.countEventCounts()
      _ <- F.delay(println(s"f COUNT Events AFTER DELETE: ${eventCountsAfter}  BOOTSTRAP PING SUCCEEDED"))
    } yield ()
  }

}
