package forex

import boot.config._
import boot.Resources
import core.rates.domains.Pair
import core.rates.domains.Rate

import org.http4s.server.blaze.BlazeServerBuilder
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import cats.effect.concurrent.Ref
import cats.effect._
import cats.implicits._

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  import org.http4s.client.Client
  import org.http4s.client.blaze.BlazeClientBuilder

  override def run(args: List[String]): IO[ExitCode] =
    load[IO]("app").flatMap { cfg =>
      Resources.create.use { res =>
        for {
          _      <- Logger[IO].info(s"Loaded config $cfg")
          module <- boot.Module[IO](cfg, res.client)
          _ <- BlazeServerBuilder[IO]
                .bindHttp(cfg.http.port, cfg.http.host)
                .withHttpApp(module.httpApp)
                .serve
                .compile
                .drain
        } yield ExitCode.Success
      }
    }
}

