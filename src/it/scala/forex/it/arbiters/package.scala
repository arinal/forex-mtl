package forex.it

import forex.core.rates.domains.Currency
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import forex.core.rates.domains.Pair

package object arbiters {

  object currencies {
    val currencyPermutations = Currency.allCombinations.toList.toSet
    val validPairsGen        = Gen.oneOf(currencyPermutations).map { case (c1, c2) => Pair(c1, c2) }
    val doublePairGen        = Gen.oneOf(Currency.values).map { case c => Pair(c, c) }

    implicit val validPairs   = Arbitrary(validPairsGen)
    implicit val invalidPairs = Arbitrary(doublePairGen)
  }
}
