package com.sa.events

import java.nio.file.Paths
import java.util.UUID

import cats.data.NonEmptySet
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import com.sa.events.api.EventWSRoutes
import com.sa.events.config.{ApiConfig, ConfHelper, DatabaseConfig, EnvConfig}
import com.sa.events.domain.eventdata.{EventCountState, EventDataService, EventDataService1}

import scala.collection.mutable
import org.http4s.server.middleware.CORS

import doobie._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._

import pureconfig.generic.auto._

import fs2.io.tcp.{SocketGroup,Socket}
/**
 * IOApp entry point for the application, sets up
 *  repo, services, routes, starts server
 */
object EventCounterMain extends IOApp{


  import cats.implicits._
  override def run(args: List[String]): IO[ExitCode] = {

    def program(blocker: Blocker) = for {
      // retrieve external config path (dev.conf or test.conf)
      envConfig <- ConfHelper.loadCnfDefault[IO, EnvConfig](EnvConfig.namespace,blocker)
      externalConfigPath = Paths.get(envConfig.getExternalConfigPath)

      // retrieve api and db info from dev or test.conf
      apiConfig <- ConfHelper.loadCnfF[IO, ApiConfig](externalConfigPath,ApiConfig.namespace,blocker)
      databaseConf <- ConfHelper.loadCnfF[IO,DatabaseConfig](externalConfigPath, DatabaseConfig.namespace, blocker)

      inetAddr = new java.net.InetSocketAddress("localhost",9999)
      _ <- EventDataService1.execute[IO](blocker,inetAddr)

//      // TODO: Need some details here, more about transactor, HikariDataSource etc.
//      xa <- PooledTransactor[IO](databaseConf)
//      _ <- IO(println(s"Got XA: $xa"))
//
//      // TODO: Need some details here, Interpreter, Algebra etc.
//      titleRepo = DoobieTitleRepositoryInterpreter[IO](xa)
//      nameRepo = DoobieNameRepositoryInterpreter[IO](xa)
//
      eventDataService = EventDataService[IO]()

      routes = createRoutes(eventDataService)

      // TODO: Need CORS info here
      corsService = CORS(routes)
      httpApp        = Router("/" -> corsService).orNotFound
      server         = BlazeServerBuilder[IO].bindHttp(apiConfig.port, apiConfig.host).withHttpApp(httpApp)
//      fiber          = server.resource.use(_ => IO(StdIn.readLine())).as(ExitCode.Success)
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

//  def createRoutes(titleService: TitleService[IO], nameService: NameService[IO]) = {
//    (new TitleRoutes(titleService)).routes <+> (new NameWSRoutes[IO](nameService)).routes
//  }

  def createRoutes(eventDataService: EventDataService[IO]) = {
    (new EventWSRoutes[IO](eventDataService)).routes
  }

}
