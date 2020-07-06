package core.rates.domains

import forex.core.rates.domains.Currency
import weaver._

object CurrencySuite extends SimpleIOSuite {

  pureTest(s"Currency 'allCombinationsLength' are correct") {
    // 14! / (14 - 2)! == 14 * 13
    val combinationLength = 182
    expect(Currency.allCombinationsLength == combinationLength)
  }
}
