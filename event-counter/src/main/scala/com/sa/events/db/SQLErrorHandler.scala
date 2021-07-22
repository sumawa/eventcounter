package com.sa.events.db

import java.sql.SQLException

import cats.data.EitherT
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import doobie._
import doobie.implicits._
import cats.syntax.functor._

/**
 * Pagination is a convenience to simply add limits and offsets to any query
 * Part of the motivation for this is using doobie's typechecker,
 */
trait SQLErrorHandler {
  def handleError[F[_],T](e: F[Either[SQLException,T]])
                    (implicit F: ConcurrentEffect[F]
                      , contextShift: ContextShift[F]
                      , timer: Timer[F])
      : EitherT[F,String,T] = {
    val c = e.map{
      case Left(ex) => Left(s"${ex.getMessage}")
      case Right(value) => Right(value)
    }
    EitherT(c)
  }}
object SQLErrorHandler extends SQLErrorHandler

