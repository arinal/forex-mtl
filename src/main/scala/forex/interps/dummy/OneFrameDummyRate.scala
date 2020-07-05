package forex
package interps.dummy

import core.rates
import core.rates.errors.Error
import rates.domains.{Pair, Price, Rate, Timestamp, Currency}

import cats.effect.Timer
import cats.effect.Sync
import cats.data.NonEmptyList
import cats.implicits._
import java.time.ZoneId

class OneFrameDummyRate[F[_]: Sync: Timer](zoneId: ZoneId) extends rates.Algebra[F] {

  override def get(pair: Pair): F[Error Either Rate] =
    for {
      now  <- Timestamp.now[F](zoneId)
      rate = Rate(pair, Price.ofInt(100), now)
    } yield rate.asRight[Error]

  override def get(pairs: NonEmptyList[Pair]): F[Error Either NonEmptyList[Rate]] =
    for {
      results <- pairs.traverse(get)
      res     = results.traverse(identity)
    } yield res

  override def allRates: fs2.Stream[F, Rate] = ???
}

object OneFrameDummyRate {
  def apply[F[_]: Sync: Timer] =
    F.delay(ZoneId.systemDefault())
      .map(new OneFrameDummyRate(_))
}
