package forex
package boot

import config.AppConfig
import app.http.rates.RatesRoutes
import interps.dummy.OneFrameDummyRate
import interps.inmemories.InmemoryRateAlg
import interps.http.PaidyOneFrameRateClient
import core.rates.domains.Pair
import core.rates.domains.Rate

import org.http4s.HttpRoutes
import org.http4s.HttpApp
import org.http4s.server.Router
import org.http4s.server.middleware._
import org.http4s.client.Client
import io.chrisdavenport.log4cats.Logger
import cats.effect._
import cats.effect.concurrent.Ref

class Module[F[_]: Sync: Concurrent: Timer: Logger](
    config: AppConfig,
    val programClientAlg: app.programs.rates.Algebra[F],
    val programInMemoryAlg: app.programs.rates.Algebra[F]
) {

  import org.http4s.implicits._

  private val routes = new RatesRoutes[F](programInMemoryAlg).routes

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
      client: Client[F],
      mapRef: Ref[F, Map[Pair, Rate]]
  ): F[Module[F]] =
    for {
      clientAlg  <- PaidyOneFrameRateClient[F](config.oneFrame.uri, client)
      clientProg = app.programs.rates.Algebra[F](clientAlg)
      inmemAlg   <- InmemoryRateAlg(mapRef)
      inmemProg = app.programs.rates.Algebra[F](inmemAlg)
    } yield new Module(config, clientProg, inmemProg)
}
