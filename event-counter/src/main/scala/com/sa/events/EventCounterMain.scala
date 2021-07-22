package com.sa.events

import java.nio.file.Paths

import cats.effect._
import com.sa.events.api.EventWSRoutes
import com.sa.events.config.{ApiConfig, ConfHelper, DatabaseConfig, EnvConfig}
import com.sa.events.domain.eventdata.{EventCountState, EventDataService}
import com.sa.events.db.RepoHelper

import org.http4s.server.middleware.CORS
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import pureconfig.generic.auto._
/**
 * IOApp entry point for the application, sets up
 *  repo, services, routes, starts server
 */
object EventCounterMain extends IOApp{

  override def run(args: List[String]): IO[ExitCode] = {

    def program(blocker: Blocker) = for {
      // retrieve external config path (dev.conf or test.conf)
      envConfig <- ConfHelper.loadCnfDefault[IO, EnvConfig](EnvConfig.namespace,blocker)
      externalConfigPath = Paths.get(envConfig.getExternalConfigPath)

      // retrieve api and db info from dev or test.conf
      apiConfig <- ConfHelper.loadCnfF[IO, ApiConfig](externalConfigPath,ApiConfig.namespace,blocker)
      databaseConf <- ConfHelper.loadCnfF[IO,DatabaseConfig](externalConfigPath, DatabaseConfig.namespace, blocker)

      repo = RepoHelper.getEventRepo[IO](databaseConf)
      _ <- RepoHelper.bootstrap[IO](repo)

//      // TODO: Need some details here, more about transactor, HikariDataSource etc.
//      xa <- PooledTransactor[IO](databaseConf)
//      _ <- IO(println(s"Got XA: $xa"))
//
//      // TODO: Need some details here, Interpreter, Algebra etc.
//      titleRepo = DoobieTitleRepositoryInterpreter[IO](xa)
//      nameRepo = DoobieNameRepositoryInterpreter[IO](xa)
//
      inetAddr = new java.net.InetSocketAddress("localhost",9999)
      eventDataService = EventDataService[IO](repo)
      _ <- eventDataService.execute(blocker,inetAddr).runAsync {
        case Left(e) => IO(e.printStackTrace())
        case Right(_) => IO.unit
      }.toIO
      routes = createRoutes(eventDataService)

      // TODO: Need CORS info here
      corsService = CORS(routes)
      httpApp        = Router("/" -> corsService).orNotFound
      server         = BlazeServerBuilder[IO].bindHttp(apiConfig.port, apiConfig.host).withHttpApp(httpApp)
      // TODO: Significance of fiber
      fiber          <- server.resource.use(_ => IO.never).as(ExitCode.Success)
    } yield fiber

    Blocker[IO].use(program(_)).attempt.unsafeRunSync match {
      case Left(e) =>
        IO {
          println("*** An error occured! ***")
          if (e ne null) {
            println(e.getMessage)
          }
          ExitCode.Error
        }
      case Right(r) => IO(r)
    }
  }

//  def createRoutes(eventDataService: EventDataService[IO]) = {
//    (new EventWSRoutes[IO](eventDataService)).routes <+> (new EventWSRoutes[IO](eventDataService)).routes
//  }

  def createRoutes(eventDataService: EventDataService[IO]) = {
    (new EventWSRoutes[IO](eventDataService)).routes
  }

}
