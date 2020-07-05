package core.rates.domains

import forex.core.rates.domains.Currency
import weaver._

object CurrencySuite extends SimpleIOSuite {

  pureTest("Combinations from currency set of 9 are 72") {
    val combinationLength = 72 // 9! / (9 - 2)!
    expect(Currency.allCombinations.length == combinationLength)
  }
}
