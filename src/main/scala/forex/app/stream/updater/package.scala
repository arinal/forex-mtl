package forex
package app
package stream

import programs.rates.Algebra
import core.rates.domains.Pair
import core.rates.domains.Rate

import io.chrisdavenport.log4cats.Logger
import cats.effect.concurrent.Ref
import cats.effect.Timer
import cats.Functor

package object updater {

  import scala.concurrent.duration._

  def streamUpdater[F[_]: Functor: Timer: Logger](
      clientRageAlg: Algebra[F],
      mapRef: Ref[F, Map[Pair, Rate]]
  ) =
    fs2.Stream
      .awakeEvery[F](1.seconds)
      .evalMap { t =>
        Logger[F].info(s"damn you av $t")
      }
}
