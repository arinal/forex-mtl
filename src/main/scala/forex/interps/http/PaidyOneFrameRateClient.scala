package forex
package interps.http

import forex.core.rates.domains.Price
import forex.core.rates.domains.Currency
import core.rates.errors
import core.rates.domains.{Pair, Rate}
import forex.core.rates.domains.Timestamp

import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.headers.Accept
import org.http4s.MediaType
import org.http4s.Status
import io.circe.Decoder
import cats.effect.Sync
import java.time.OffsetDateTime

class PaidyOneFrameRateClient[F[_]: Sync](uri: Uri, client: Client[F])
    extends core.rates.Algebra[F]
    with Http4sClientDsl[F] {

  import PaidyOneFrameRateClient._
  import cats.implicits._
  import org.http4s.circe._

  override def get(pair: Pair): F[errors.Error Either Rate] = {
    for {
      either <- invoke(List(pair))
      res = either
        .map { rates =>
          val rate = rates.head
          Rate(Pair(rate.from, rate.to), Price(rate.price), Timestamp(rate.time_stamp))
        }
        .leftMap {
          case ErrorResponse("Invalid Currency Pair") => errors.Error.CurrencyNotSupported
          case ErrorResponse(msg)                     => errors.Error.OneFrameLookupFailed(msg)
        }
    } yield res
  }

  def invoke(pairs: List[Pair]): F[ErrorResponse Either List[RateResponse]] = {
    client.fetch[ErrorResponse Either List[RateResponse]](
      GET(uri, Accept(MediaType.application.json))
    ) { res =>
      if (res.status == Status.Ok) {
        res.asJsonDecode[List[RateResponse]].attempt.flatMap {
          case Right(r) => r.asRight[ErrorResponse].pure[F]
          case Left(_)  => res.asJsonDecode[ErrorResponse].map(Left(_))
        }
      } else ErrorResponse("Unexpected error").asLeft[List[RateResponse]].pure[F]
    }
  }
}

object PaidyOneFrameRateClient {

  implicit val rateResponseDec: Decoder[RateResponse] = ???
  implicit val errResponseDec: Decoder[ErrorResponse] = ???

  final case class RateResponse(
      from: Currency,
      to: Currency,
      bid: BigDecimal,
      ask: BigDecimal,
      price: BigDecimal,
      time_stamp: OffsetDateTime
  )

  final case class ErrorResponse(error: String)
}
