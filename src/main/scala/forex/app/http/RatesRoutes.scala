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

  import commons.http._
  import protocols._
  import converters._
  import cats.implicits._
  import errors.Error

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromParam(fromOpt) +& ToParam(toOpt) =>
      val result = for {
        from      <- Sync[F].fromOption(fromOpt, errors.Error.CurrencyNotSupported)
        to        <- Sync[F].fromOption(toOpt, errors.Error.CurrencyNotSupported)
        rateOrErr <- rateAlg.get(GetRatesRequest(from, to))
        rate      <- Sync[F].fromEither(rateOrErr)
        res       <- Ok(rate.toResponse)
      } yield res

      result.handleErrorWith {
        case Error.CurrencyNotSupported =>
          BadRequest("Currency is not supported.")
        case Error.RateLookupFailed(msg) =>
          InternalServerError(s"Rate lookup failed: $msg")
      }
  }

  val routes = Router("/rates" -> httpRoutes)
}
