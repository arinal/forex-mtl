package forex.core.rates

object errors {

  sealed trait Error

  object Error {
    final case class CurrencyNotSupported(requestedCurr: Option[String] = None) extends Error
    final case class OneFrameLookupFailed(msg: String) extends Error
  }
}
