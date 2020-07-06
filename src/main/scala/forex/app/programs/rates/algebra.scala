package forex
package app.programs.rates

import errors.Error
import core.rates.domains.Rate
import forex.core.rates.domains.Pair
import scala.concurrent.duration.FiniteDuration

import cats.Functor

trait Algebra[F[_]] {

  import scala.concurrent.duration._

  def get(request: protocol.GetRatesRequest): F[Error Either Rate]
  def allRates: fs2.Stream[F, Rate]

  /**
   * How much time to wait between invocation to one frame server given max invocations
   * they preserve?
   * @param maxInvocations maximum served requests allowed for OneFrame server.
   * @return duration to wait for every update
  **/
  def updateEvery(maxInvocations: Int): FiniteDuration =
    (60 * 60 * 24.0 / maxInvocations).seconds
}

object Algebra {

  import cats.implicits._

  def apply[F[_]: Functor](rateAlg: core.rates.Algebra[F]): Algebra[F] =
    new Algebra[F] {

      override def get(request: protocol.GetRatesRequest): F[Error Either Rate] =
        for {
          rateRes <- rateAlg.get(Pair(request.from, request.to))
          appRes  = rateRes.leftMap(errors.toProgramsError)
        } yield appRes

      override def allRates: fs2.Stream[F, Rate] = rateAlg.allRates
    }
}
