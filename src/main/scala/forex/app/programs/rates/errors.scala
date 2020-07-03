package forex.app.programs.rates

import forex.core.rates.errors.{ Error => CoreError }

object errors {

  sealed trait Error extends Exception
  object Error {
    final case class RateLookupFailed(msg: String) extends Error
  }

  def toProgramsError(error: CoreError): Error = error match {
    case CoreError.OneFrameLookupFailed(msg) => Error.RateLookupFailed(msg)
  }
}
