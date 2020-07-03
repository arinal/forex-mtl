package forex
package interps.dummy

import core.rates
import core.rates.errors.Error
import rates.domains.{ Pair, Price, Rate, Timestamp }
import cats.effect.Timer
import cats.effect.Sync
import java.time.ZoneId

import cats.implicits._

class OneFrameDummyRate[F[_]: Sync: Timer](zoneId: ZoneId) extends rates.Algebra[F] {

  override def get(pair: Pair): F[Error Either Rate] =
    for {
      now <- Timestamp.now[F](zoneId)
      rate = Rate(pair, Price.ofInt(100), now)
    } yield rate.asRight[Error]
}

object OneFrameDummyRate {
  def apply[F[_]: Sync: Timer] =
    Sync[F]
      .delay(ZoneId.systemDefault())
      .map(new OneFrameDummyRate(_))
}
