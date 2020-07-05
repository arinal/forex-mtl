package forex

import config.AppConfig
import app.http.RatesRoutes
import interps.dummy.OneFrameDummyRate
import interps.http.PaidyOneFrameRateClient

import org.http4s.HttpRoutes
import org.http4s.HttpApp
import org.http4s.server.Router
import org.http4s.server.middleware._
import org.http4s.client.Client
import io.chrisdavenport.log4cats.Logger
import cats.effect._

class Module[F[_]: Sync: Concurrent: Timer: Logger](
    config: AppConfig,
    programAlg: app.programs.rates.Algebra[F]
) {

  import org.http4s.implicits._

  private val routes = new RatesRoutes[F](programAlg).routes

  private val middleware: HttpRoutes[F] => HttpRoutes[F] =
    { http: HttpRoutes[F] => AutoSlash(http)                    } andThen
    { http: HttpRoutes[F] => CORS(http, CORS.DefaultCORSConfig) } andThen
    { http: HttpRoutes[F] => Timeout(config.http.timeout)(http) }

  private val loggers: HttpApp[F] => HttpApp[F] =
    { http: HttpApp[F] => RequestLogger.httpApp(true, true)(http)  } andThen
    { http: HttpApp[F] => ResponseLogger.httpApp(true, true)(http) }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)
}

object Module {

  import org.http4s._
  import org.http4s.implicits._
  import cats.implicits._

  def apply[F[_]: ContextShift: ConcurrentEffect: Timer: Logger](
      config: AppConfig,
      client: Client[F]
  ): F[Module[F]] =
    for {
      rateAlg <-
        // OneFrameDummyRate[F]
        PaidyOneFrameRateClient[F](config.oneFrameUri, client)
      program = app.programs.rates.Algebra[F](rateAlg)
    } yield new Module(config, program)
}
