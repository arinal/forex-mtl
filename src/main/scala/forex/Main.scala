package forex

import boot.config._
import boot.Resources
import core.rates.domains.Pair
import core.rates.domains.Rate

import org.http4s.server.blaze.BlazeServerBuilder
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import cats.effect.concurrent.Ref
import cats.effect._
import cats.implicits._

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    load[IO]("").flatMap { cfg =>
      Resources.create.use { res =>
        for {
          _   <- Logger[IO].info(s"Loaded config $cfg")
          mod <- boot.Module[IO](cfg, res.client, res.rateRef)

          _ <- app.stream.updater
                .stream(
                  mod.programInMemoryAlg.updateEvery(cfg.oneFrame.maxInvocations),
                  mod.programClientAlg,
                  res.rateRef
                )
                .compile
                .drain
                .start
          _ <- BlazeServerBuilder[IO]
                .bindHttp(cfg.http.port, cfg.http.host)
                .withHttpApp(mod.httpApp)
                .serve
                .compile
                .drain
        } yield ExitCode.Success
      }
    }
}
