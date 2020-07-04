package forex

import org.http4s.server.blaze.BlazeServerBuilder
import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      cfg    <- config.load[IO]("app")
      _      <- Logger[IO].info(s"Loaded config $cfg")
      module <- Module[IO](cfg)
      _ <- BlazeServerBuilder[IO]
            .bindHttp(cfg.http.port, cfg.http.host)
            .withHttpApp(module.httpApp)
            .serve
            .compile
            .drain
    } yield ExitCode.Success
}
