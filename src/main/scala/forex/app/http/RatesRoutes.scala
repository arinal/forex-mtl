package forex.app
package http

import programs.rates.protocol.GetRatesRequest

import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.HttpRoutes
import cats.Defer
import cats.effect.Sync

class RatesRoutes[F[_]: Sync](rateAlg: programs.rates.Algebra[F]) extends Http4sDsl[F] {

  import commons.http._
  import protocols._
  import converters._
  import cats.implicits._

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromParam(from) +& ToParam(to) =>
      rateAlg
        .get(GetRatesRequest(from, to))
        .flatMap(Sync[F].fromEither)
        .flatMap { rate =>
          Ok(rate.toResponse)
        }
  }

  val routes = Router("/rates" -> httpRoutes)
}
