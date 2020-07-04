package forex
package app.programs.rates

import errors.Error
import core.rates.domains.Rate
import forex.core.rates.domains.Pair

import cats.Functor

trait Algebra[F[_]] {
  def get(request: protocol.GetRatesRequest): F[Error Either Rate]
}

object Algebra {

  import cats.implicits._

  def apply[F[_]: Functor](rateAlg: core.rates.Algebra[F]): Algebra[F] =
    new Algebra[F] {
      override def get(request: protocol.GetRatesRequest): F[Error Either Rate] =
        for {
          rateRes <- rateAlg.get(Pair(request.from, request.to))
          appRes = rateRes.leftMap(errors.toProgramsError)
        } yield appRes
    }
}
