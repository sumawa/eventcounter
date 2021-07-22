package com.sa.imdb.domain.meta

import java.sql.SQLException

import cats.data.EitherT
import com.sa.events.domain.eventdata.EventCount

import scala.collection.mutable

/*
  trait exposing functions for interacting with DB Repo
 */
trait EventDataRepositoryAlgebra[F[_]] {

  def createTables(): F[Int]

  def updateEventCountMap(ecMap: mutable.Map[String,Int]): F[Int]

  def insertEventCounts(eventCounts: List[(String,Int)]): F[Int]

  def getEventData(): F[Seq[EventCount]]

  def deleteEventCountByType(eventType: String): F[Int]

  def countEventCounts(): F[Seq[Int]]
}
