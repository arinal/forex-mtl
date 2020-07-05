package forex
package interps.http

import core.rates.domains.{Pair, Rate, Timestamp, Price, Currency}
import core.rates.errors

import org.http4s.Uri
import org.http4s.Query
import org.http4s.MediaType
import org.http4s.Status
import org.http4s.Method._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Accept
import io.circe.Decoder
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.Applicative
import cats.implicits._
import java.time.OffsetDateTime

object protocols {

  import io.circe.generic.semiauto._

  final case class RateResponse(
      from: Currency,
      to: Currency,
      bid: BigDecimal,
      ask: BigDecimal,
      price: BigDecimal,
      time_stamp: OffsetDateTime
  ) {
    def toDomain =
      Rate(Pair(from, to), Price(price), Timestamp(time_stamp))
  }

  final case class ErrorResponse(error: String) {
    def toDomain: errors.Error = error match {
      case "Invalid Currency Pair" => errors.Error.CurrencyNotSupported()
      case msg                     => errors.Error.RateLookupFailed(msg)
    }
  }

  implicit val currencyDec: Decoder[Currency] = Decoder[String].emap { currName =>
    Currency.fromString(currName).leftMap(_ => s"Currency $currName is not supported")
  }

  implicit val rateResponseDec: Decoder[RateResponse] = deriveDecoder[RateResponse]
  implicit val errResponseDec: Decoder[ErrorResponse] = deriveDecoder[ErrorResponse]
}
