package com.sa.events
import java.nio.file.Paths

import cats.effect.{Blocker, ContextShift, IO, Timer}
import com.sa.events.config.{ApiConfig, ConfHelper, DatabaseConfig, EnvConfig}
import com.sa.events.db.PooledTransactor
import com.sa.events.domain.eventdata.EventDataService
import com.sa.events.config.ConfHelper
import com.sa.events.domain.eventdata.EventDataService
import com.sa.tickets.db.DoobieEventDataRepositoryInterpreter
import fs2.Stream
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.scalatest.{BeforeAndAfter, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.{FeatureSpec, GivenWhenThen}
import pureconfig.generic.auto._

trait FeatureBaseSpec extends FeatureSpec with Matchers with BeforeAndAfter with ScalaCheckDrivenPropertyChecks with GivenWhenThen {
  implicit val glo = scala.concurrent.ExecutionContext.Implicits.global
  implicit val cs: ContextShift[IO] = IO.contextShift(glo)
  implicit val timer: Timer[IO] = IO.timer(glo)

  System.setProperty("EC_ENV","test")
  before {
  }
  after {
    println(s"CLEANING UP TEST DB &&&&&&&&&&&&&&")
  }

  def getEventDataService(blocker: Blocker) = for {
    envConfig <- ConfHelper.loadCnfDefault[IO, EnvConfig](EnvConfig.namespace,blocker)
    externalConfigPath = Paths.get(envConfig.getExternalConfigPath)

    // retrieve api and db info from dev or test.conf
    apiConfig <- ConfHelper.loadCnfF[IO, ApiConfig](externalConfigPath,ApiConfig.namespace,blocker)
    databaseConf <- ConfHelper.loadCnfF[IO,DatabaseConfig](externalConfigPath, DatabaseConfig.namespace, blocker)

    // TODO: Need some details here, more about transactor, HikariDataSource etc.
    xa <- PooledTransactor[IO](databaseConf)
    _ <- IO(println(s"Got XA: $xa"))
    eventRepo = DoobieEventDataRepositoryInterpreter[IO](xa)
    eventDataService = new EventDataService[IO](eventRepo)

  } yield eventDataService

  val eventDataService = (Blocker[IO]).use(getEventDataService(_)).unsafeRunSync()
}