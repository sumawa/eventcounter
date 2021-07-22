package com.sa.events.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

/**
 * The configuration for scheduler
 *
 * @param interval periodic reading from socket via stream is controlled by this value (in seconds_
 */
final case class SchedulerConfig(interval: Int)

object SchedulerConfig {

  val namespace: String = "scheduler"
  implicit val configReader: ConfigReader[SchedulerConfig] = deriveReader[SchedulerConfig]

}