package forex.app
package http

import programs.rates.protocol.GetRatesRequest
import programs.rates.errors

import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.HttpRoutes
import cats.Defer
import cats.effect.Sync

class RatesRoutes[F[_]: Sync](rateAlg: programs.rates.Algebra[F]) extends Http4sDsl[F] {

  import protocols._
  import converters._
  import commons.http._
  import cats.implicits._
  import errors.Error

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromParam(fromEith) +& ToParam(toEith) =>
      val result = for {
        from      <- Sync[F].fromEither(fromEith)
        to        <- Sync[F].fromEither(toEith)
        rateOrErr <- rateAlg.get(GetRatesRequest(from, to))
        rate      <- Sync[F].fromEither(rateOrErr)
        res       <- Ok(rate.toResponse)
      } yield res

      result.handleErrorWith {
        case Error.CurrencyNotSupported(curr) =>
          BadRequest(s"Currency $curr is not supported.")
        case Error.RateLookupFailed(msg) =>
          InternalServerError(s"Rate lookup failed: $msg")
      }
  }

  val routes = Router("/rates" -> httpRoutes)
}
