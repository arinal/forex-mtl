package forex.core.rates

import domains.Pair
import domains.Rate

trait Algebra[F[_]] {
  def get(pair: Pair): F[errors.Error Either Rate]
}
