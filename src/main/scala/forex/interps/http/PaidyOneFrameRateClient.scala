package forex
package interps.http

import core.rates.domains.{Pair, Rate, Timestamp, Price, Currency}
import core.rates.errors

import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.headers.Accept
import org.http4s.MediaType
import org.http4s.Status
import io.circe.Decoder
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.Applicative
import cats.implicits._
import java.time.OffsetDateTime

class PaidyOneFrameRateClient[F[_]: Sync](uri: Uri, client: Client[F])
    extends core.rates.Algebra[F]
    with Http4sClientDsl[F] {

  import PaidyOneFrameRateClient._
  import org.http4s.circe._

  override def get(pair: Pair): F[errors.Error Either Rate] =
    for {
      either <- get(NonEmptyList.one(pair))
      res    = either.map(_.head)
    } yield res

  override def get(pairs: NonEmptyList[Pair]): F[errors.Error Either NonEmptyList[Rate]] = {
    val resp = client.fetch[ErrorResponse Either NonEmptyList[RateResponse]](
      GET(uri, Accept(MediaType.application.json))
    ) { res =>
      if (res.status == Status.Ok) {
        res.asJsonDecode[NonEmptyList[RateResponse]].attempt.flatMap {
          case Right(r) => r.asRight.pure[F]
          case Left(_)  => res.asJsonDecode[ErrorResponse].map(Left(_))
        }
      } else ErrorResponse("Unexpected error").asLeft.pure[F]
    }
    resp.map {
      case Right(rateRespList) => rateRespList.map(_.toDomain).asRight
      case Left(err)           => err.toDomain.asLeft
    }
  }
}

object PaidyOneFrameRateClient {

  import io.circe.generic.semiauto._

  def apply[F[_]: Sync](uri: Uri, client: Client[F]): F[PaidyOneFrameRateClient[F]] =
    Sync[F].delay(new PaidyOneFrameRateClient[F](uri, client))

  implicit val currencyDec: Decoder[Currency] = Decoder[String].emap { currName =>
    Currency.fromString(currName).leftMap(_ => s"Currency $currName is not supported")
  }

  implicit val rateResponseDec: Decoder[RateResponse] = deriveDecoder[RateResponse]
  implicit val errResponseDec: Decoder[ErrorResponse] = deriveDecoder[ErrorResponse]

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
      case msg                     => errors.Error.OneFrameLookupFailed(msg)
    }
  }
}
