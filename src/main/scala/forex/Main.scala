package forex

import org.http4s.server.blaze.BlazeServerBuilder
import cats.effect._
import cats.implicits._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      cfg <- config.load[IO]("app")
      module <- Module[IO](cfg)
      _ <- BlazeServerBuilder[IO]
            .bindHttp(cfg.http.port, cfg.http.host)
            .withHttpApp(module.routes)
            .serve
            .compile
            .drain
    } yield ExitCode.Success
}
