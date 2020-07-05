package forex.app.programs.rates

import forex.core.rates.errors.{Error => CoreError}
import forex.core.rates.domains.Currency
import scala.util.control.NoStackTrace

object errors {

  sealed trait Error extends NoStackTrace

  object Error {
    final case class CurrencyNotSupported(requestedCurrency: Option[String]) extends Error
    final case class RateLookupFailed(msg: String)                           extends Error
  }

  def toProgramsError(error: CoreError): Error = error match {
    case CoreError.OneFrameLookupFailed(msg)  => Error.RateLookupFailed(msg)
    case CoreError.CurrencyNotSupported(curr) => Error.CurrencyNotSupported(curr)
  }
}
