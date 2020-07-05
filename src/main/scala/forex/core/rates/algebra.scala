package forex.core.rates

import domains.{Pair, Rate}
import cats.data.NonEmptyList

trait Algebra[F[_]] {

  /**
   * @param pair pair of currencies (from, to) which rate to be calculated
   * @return a [[Rate]], which includes currency pairs, rate, and timestamp
  **/
  def get(pair: Pair): F[errors.Error Either Rate]

  /**
   * Get batch of rates for currencies requested by the input.
   * Protip: some of the implementations of currency rate calculator might limit the rate requests
   * it serves in a given time window (e.g. 1000 requests per day). Using this interface
   * will only counted as one request.
   * @param pairs list of [[Pair]] which currencies to be calculated
   * @return list of [[Rate]]
  **/
  def get(pairs: NonEmptyList[Pair]): F[errors.Error Either NonEmptyList[Rate]]

  /**
   * Get all rates for every combinations of supported currencies.
   * Even though more aggressive than its get pairs counterparts, this also might counted as 1 request.
   * @return stream of all possible combinations of [[Rate]]
  **/
  def allRates: fs2.Stream[F, Rate]
}
