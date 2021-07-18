//package com.sa.events.domain.titles
//
//import java.sql.SQLException
//
//import cats.data.{EitherT, NonEmptySet}
//import cats.Order
//import cats.effect.ConcurrentEffect
//import io.circe.{Decoder, Encoder, Printer}
//import io.circe.generic.semiauto._
//
//case class Title(tconst: String, primaryTitle: String, originalTitle: String
//                 , titleType: String, genres: Option[String], startYear: Option[Int], averageRating: Option[Double], numVotes: Option[Int], castAndCrew: String)
//
//object Title{
//  type AppE[T] = Either[String,T]
//  type AppEx[T] = Either[SQLException,T]
//  implicit val decode: Decoder[Title] = deriveDecoder[Title]
//  implicit val encode: Encoder[Title] = deriveEncoder[Title]
//}
