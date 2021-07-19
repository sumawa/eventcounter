package com.sa.events.domain.eventdata

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import scala.collection.mutable

case class EventData(event_type: String, data: String, timestamp: Long)

object EventData{
  //    type EventE[T] = Either[String,T]
  //    type EventEx[T] = Either[SQLException,T]
  implicit val decode: Decoder[EventData] = deriveDecoder[EventData]
  implicit val encode: Encoder[EventData] = deriveEncoder[EventData]
}

/*
   A wrapper class containing all mutable states.
  */
case class EventCountState(map: mutable.Map[String, Int])

object EventCountState{
  implicit val decode: Decoder[EventCountState] = deriveDecoder[EventCountState]
  implicit val encode: Encoder[EventCountState] = deriveEncoder[EventCountState]
}

