package forex

import org.http4s.server.blaze.BlazeServerBuilder

import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import forex.config.AppConfig

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  import org.http4s.client.Client
  import org.http4s.client.blaze.BlazeClientBuilder
  import scala.concurrent.ExecutionContext

  def mkResource[F[_]: ContextShift: ConcurrentEffect](config: AppConfig): Resource[F, Client[F]] =
    BlazeClientBuilder[F](ExecutionContext.global).resource

  override def run(args: List[String]): IO[ExitCode] =
    config.load[IO]("app").flatMap { cfg =>
      mkResource(cfg).use { client =>
        for {
          cfg    <- config.load[IO]("app")
          _      <- Logger[IO].info(s"Loaded config $cfg")
          module <- Module[IO](cfg, client)
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
