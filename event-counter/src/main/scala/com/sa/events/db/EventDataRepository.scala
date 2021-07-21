package com.sa.events.db

import java.util.UUID

//import com.sa.tickets.models.{Show}
import fs2.Stream


/**
 * A base class for our database repository.
 *
 * @tparam F A higher kinded type which wraps the actual return value
 *           (effects like IO, Sync, Async, etc)
 */
trait EventDataRepository[F[_]] {

  /**
   * create schema
   * @return
   */
  def createTables(): F[Int]

}
