package forex.core.rates
package domains

import forex.core.rates.errors.Error.CurrencyNotSupported
import cats.data.NonEmptyList
import cats.Show
import enumeratum._
import scala.util.Try

sealed trait Currency extends EnumEntry

object Currency extends Enum[Currency] with CatsEnum[Currency] {

  val values = findValues

  case object AUD extends Currency
  case object CAD extends Currency
  case object CHF extends Currency
  case object EUR extends Currency
  case object GBP extends Currency
  case object NZD extends Currency
  case object JPY extends Currency
  case object SGD extends Currency
  case object USD extends Currency

  import cats.implicits._

  def fromString(currencyName: String): errors.Error Either Currency =
    Currency.withNameInsensitiveEither(currencyName).leftMap {
      case _ => CurrencyNotSupported(currencyName.some)
    }

  def allCombinations: NonEmptyList[(Currency, Currency)] = {
    val pairs = values
      .permutations
      .map { pair => (pair(0), pair(1)) }
      .distinct
      .toList

    // fromListUnsafe is safe here, unless pairs is empty.
    // We can guarantee pairs unemptiness since findValues result
    // is hardcoded (enum members)
    NonEmptyList.fromListUnsafe(pairs)
  }
}
