package com.sa.imdb.domain.meta

import java.sql.SQLException

import cats.data.EitherT
import com.sa.events.domain.eventdata.EventCount

import scala.collection.mutable

/*
  trait exposing functions for interacting with DB Repo
 */
trait EventDataRepositoryAlgebra[F[_]] {

  def createTables(): EitherT[F,String,Int]

  def updateEventCountMap(ecMap: mutable.Map[String,Int]): EitherT[F,String,Int]

  def insertEventCounts(eventCounts: List[(String,Int)]): EitherT[F,String,Int]

  def getEventData(): EitherT[F,String,Seq[EventCount]]

  def deleteEventCountByType(eventType: String): EitherT[F,String,Int]

  def countEventCounts(): EitherT[F,String,Seq[Int]]

}
