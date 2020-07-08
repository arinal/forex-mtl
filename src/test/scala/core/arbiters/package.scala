package core

import org.scalacheck.Gen
import forex.core.rates.domains.Currency
import org.scalacheck.Arbitrary

package object arbiters {

  object currencies {
    val currencyPermutations = Currency.allCombinations.toList.toSet
    val validPairsGen        = Gen.oneOf(currencyPermutations)

    implicit val permutationsArb = Arbitrary(validPairsGen)
  }
}
