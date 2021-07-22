package com.sa.tickets.db

import java.sql.SQLException
import java.util.UUID

import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.free.Free

import com.sa.events.domain.eventdata.EventCount
import com.sa.imdb.domain.meta.EventDataRepositoryAlgebra
import doobie._
import doobie.free.connection.ConnectionOp
import doobie.implicits._

import scala.collection.mutable

/**
 * - With Doobie we write plain SQL queries.
 * - We can use something like Quill for Object DB mapping
 * - For ORM or other forms of query compilers then this may seem strange at first. But: “In data processing it seems, all roads eventually lead back to SQL!”
 * - Using the approach of using the de facto lingua franca for database access because it was made for this
 * - and so far no query compiler was able to beat hand crafted SQL in terms of performance.
 *
 * Keeping higher kinded type abstract F
 * To be able to suspend side effects, we are using implicit Sync
 *
 * @param xa A transactor for actually executing our queries.
 * @tparam F A higher kinded type which wraps the actual return value
 *           (effects like IO, Sync, Async, etc)
 */
class DoobieEventDataRepositoryInterpreter[F[_]](val xa: Transactor[F])
                                                (implicit F: ConcurrentEffect[F]
                                                 , contextShift: ContextShift[F]
                                                , timer: Timer[F]) extends EventDataRepositoryAlgebra[F] {

  def createTables() = {
    println(s"CREATING TABLES")

    val createSql =
      sql"""
           |CREATE TABLE IF NOT EXISTS event_counts  (
           |  event_type    VARCHAR(10000) NOT NULL,
           |  event_count         INT DEFAULT 0,
           |  PRIMARY KEY (event_type)
           |);
           |
           |""".stripMargin

    val prog: Free[ConnectionOp, Int] = for {
      created <- createSql.update.run
    } yield created

    prog.transact(xa)
  }

  override def updateEventCountMap(ecMap: mutable.Map[String,Int]): F[Int] = {
    val eventCounts = ecMap.foldLeft(List[(String,Int)]()){ case(acc, (k,v)) =>
      (k,v) :: acc
    }
    println(s"UPDATING EVENT COUNTS AFTER DELETING : ${ecMap.size}")

    val list = eventCounts.map(_._1)
    val updateSql = "INSERT into event_counts (event_type,event_count) values (?,?)"

    import cats.syntax.all._
    val delQ = fr"""
    DELETE
    FROM event_counts
    WHERE """ ++ Fragments.in(fr"event_type", list.toNel.get)

    val prog: Free[ConnectionOp, Int] = for {
      di <- delQ.update.run
      si <- Update[(String, Int)](updateSql, None, LogHandler.jdkLogHandler)
        .updateMany(eventCounts)
    } yield (si)
    val res = prog.transact(xa)
    println(s"AFTER UPDATING Event counts MAP: ${res}")
    res
  }

  /**
   * Save the given event counts in the database.
   *
   * @param eventCounts list of event counts to be saved
   * @return The number of affected database rows of shows.
   */
//  override def updateEventCounts(eventCounts: List[EventCount]): EitherT[F,SQLException,Int] = {
  override def insertEventCounts(eventCounts: List[(String,Int)]): F[Int] = {
    println(s"INSERTING EVENT COUNTS: ${eventCounts.size}")

    val list = eventCounts.map(_._1)
    val updateSql = "INSERT into event_counts (event_type,event_count) values (?,?)"

    import cats.syntax.all._
    val delQ = fr"""
    DELETE
    FROM event_counts
    WHERE """ ++ Fragments.in(fr"event_type", list.toNel.get)

    val prog: Free[ConnectionOp, Int] = for {
      di <- delQ.update.run
      si <- Update[(String, Int)](updateSql, None, LogHandler.jdkLogHandler)
        .updateMany(eventCounts)
    } yield (si)
    val res = prog.transact(xa)
    println(s"AFTER UPDATING Event counts INSERT: ${res}")
    res
  }

  override def getEventData(): F[Seq[EventCount]] = {
    println(s"GETTING EVENT DATA")
    sql"""
    SELECT event_type, event_count
    FROM event_counts
    """.queryWithLogHandler[EventCount](LogHandler.jdkLogHandler)
      .to[Seq]
      .transact(xa)

  }

  override def countEventCounts(): F[Seq[Int]] = {
    val res = sql"""SELECT count(event_type)
          FROM event_counts"""
      .query[Int]
      .to[Seq]
      .transact(xa)
    println(s"counting number of event counts: ${res}")
    res
  }

  override def deleteEventCountByType(eventType: String): F[Int] = {
    val prog: Free[ConnectionOp, Int] = for {
      dl <- sql"DELETE FROM event_counts WHERE event_type = ${eventType}".update.run
    } yield (dl)
    prog.transact(xa)
  }
}

object DoobieEventDataRepositoryInterpreter {
  def apply[F[_]: ConcurrentEffect: ContextShift: Timer](
                                             xa: Transactor[F],
                                           ): DoobieEventDataRepositoryInterpreter[F] =
    new DoobieEventDataRepositoryInterpreter[F](xa)

}



