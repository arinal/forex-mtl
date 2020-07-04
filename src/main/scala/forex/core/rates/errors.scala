package forex.core.rates

object errors {

  sealed trait Error

  object Error {
    final case object CurrencyNotSupported extends Error
    final case class OneFrameLookupFailed(msg: String) extends Error
  }
}
