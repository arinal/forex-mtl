package forex
package app.http

import core.rates.domains._

import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import io.circe._
import io.circe.generic.semiauto._

object protocols {

  final case class RateRequest(from: Currency, to: Currency)
  final case class RateResponse(from: Currency, to: Currency, price: Price, timestamp: Timestamp)

  implicit val currencyEncoder: Encoder[Currency] = Encoder.instance[Currency] {
    Currency.show.show _ andThen Json.fromString
  }

  implicit val pairEncoder: Encoder[Pair]             = deriveEncoder[Pair]
  implicit val priceEncoder: Encoder[Price]           = deriveEncoder[Price]
  implicit val rateEncoder: Encoder[Rate]             = deriveEncoder[Rate]
  implicit val timeStampEncoder: Encoder[Timestamp]   = deriveEncoder[Timestamp]
  implicit val responseEncoder: Encoder[RateResponse] = deriveEncoder[RateResponse]

  private[http] implicit val currencyParam = QueryParamDecoder[String].map(Currency.fromString)

  object FromParam extends QueryParamDecoderMatcher[Currency]("from")
  object ToParam extends QueryParamDecoderMatcher[Currency]("to")
}
