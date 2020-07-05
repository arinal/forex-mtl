package forex.core.rates.domains

import cats.data.NonEmptyList

case class Rate(pair: Pair, price: Price, timestamp: Timestamp)
final case class Pair(from: Currency, to: Currency)

object Rate {

  def pairFromTuple(tuple: (Currency, Currency)): Pair =
    Pair(tuple._1, tuple._2)

  def allCurrencyPairs: NonEmptyList[Pair] =
    Currency.allCombinations.map(pairFromTuple)
}
