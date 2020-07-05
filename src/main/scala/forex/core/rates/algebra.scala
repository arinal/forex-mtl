package forex.core.rates

import domains.Pair
import domains.Rate
import cats.data.NonEmptyList

trait Algebra[F[_]] {
  def get(pair: Pair): F[errors.Error Either Rate]
  def get(pairs: NonEmptyList[Pair]): F[errors.Error Either NonEmptyList[Rate]]
}
