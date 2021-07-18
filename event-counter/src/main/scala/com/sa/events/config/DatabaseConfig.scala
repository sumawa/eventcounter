package com.sa.events.config

import io.circe.syntax._

case class DatabaseConfig(
                           host: String,
                           port: Int,
                           name: String,
                           user: String,
                           pass: String,
                           `type`: String,
                           poolSize: Int,
                           config: Map[String, String] = Map()
                         ) {

  def driver: String =
    `type`.toLowerCase match {
      case "mysql"      => "com.mysql.jdbc.Driver"
      case "postgresql" => "org.postgresql.Driver"
      case "sqlserver"  => "com.microsoft.jdbc.sqlserver.SQLServerDriver"
//      case "h2"         => ""
    }

  def jdbcUrl: String =
    s"jdbc:${`type`}://$host:$port/$name"

  override def toString: String =
    s"jdbcUrl: $jdbcUrl # user: $user # config: ${config.asJson.noSpaces} "
}

object DatabaseConfig {
  val namespace: String = "database"
}
