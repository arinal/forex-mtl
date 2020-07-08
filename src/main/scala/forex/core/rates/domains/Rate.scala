package forex.core.rates.domains

import cats.data.NonEmptyList
import cats.Show

case class Rate(pair: Pair, price: Price, timestamp: Timestamp)
final case class Pair private (from: Currency, to: Currency)

object Rate {

  implicit val pairShow = Show.show[Pair](p => s"${p.from}${p.to}")

  def allCurrencyPairs: NonEmptyList[Pair] =
    Currency.allCombinations.map { case (c1, c2) => Pair(c1, c2) }
}
