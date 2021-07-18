//package com.sa.events.domain.meta
//
//import cats.data.NonEmptySet
//import cats.Order
//import io.circe.{Decoder, Encoder, Printer}
//import io.circe.generic.semiauto._
//import NameState._
//
//case class NameState(idAndName: IdAndName, deg: Int, found: Boolean)
//
//object NameState{
//  type IdAndName = (String,String)
//  implicit val decode: Decoder[NameState] = deriveDecoder[NameState]
//  implicit val encode: Encoder[NameState] = deriveEncoder[NameState]
//
//}
