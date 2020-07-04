package forex

import config.AppConfig
import app.http.RatesRoutes
import interps.dummy.OneFrameDummyRate

import org.http4s.HttpRoutes
import org.http4s.HttpApp
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.Timeout
import org.http4s.server.middleware.AutoSlash
import org.http4s.server.middleware.RequestLogger
import org.http4s.server.middleware.ResponseLogger
import cats.effect.Sync
import cats.effect.Timer
import cats.effect.Concurrent

class Module[F[_]: Sync: Concurrent: Timer](config: AppConfig, programAlg: app.programs.rates.Algebra[F]) {

  import scala.concurrent.duration._
  import org.http4s.implicits._

  private val routes = new RatesRoutes[F](programAlg).routes

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = { http: HttpRoutes[F] =>
    AutoSlash(http)
  } andThen { http: HttpRoutes[F] =>
    CORS(http, CORS.DefaultCORSConfig)
  } andThen { http: HttpRoutes[F] => Timeout(config.http.timeout)(http) }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] => RequestLogger.httpApp(true, true)(http) } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(true, true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)
}

object Module {

  import cats.implicits._
  import org.http4s._
  import org.http4s.implicits._

  def apply[F[_]: Sync: Concurrent: Timer](config: AppConfig) =
    for {
      rateAlg <- OneFrameDummyRate[F]
      program = app.programs.rates.Algebra[F](rateAlg)
    } yield new Module(config, program)
}
