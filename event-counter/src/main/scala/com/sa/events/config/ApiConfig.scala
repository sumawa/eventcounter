package com.sa.events.config

import pureconfig._
import pureconfig.generic.semiauto._

/**
 * The configuration for our HTTP API.
 *
 * @param host The hostname or ip address on which the service shall listen.
 * @param port The port number on which the service shall listen.
 */
final case class ApiConfig(host: String, port: Int)

/*
  The implicits in the companion objects are needed for pureconfig to actually map
  from a configuration to out data types.
  We are using a function deriveReader which will derive
  (like in mathematics) the codec (Yes, it is similar to a JSON codec thus the name.).

  A note on derivation
  - In general we always want the compiler to derive stuff automatically because it means less work for us.
  - However, as always there is a cost and sometimes a rather big one (compile time).
  - Therefore you should not use fully automatic derivation but the semi automatic variant instead.
  - The latter will let you chose what to derive explicitly.
  - In some circumstances it may even be better to generate a codec manually (complex, deeply nested models).
 */
object ApiConfig {

  val namespace: String = "api"
  implicit val configReader: ConfigReader[ApiConfig] = deriveReader[ApiConfig]

}