package forex.core.rates.domains

case class Rate(pair: Pair, price: Price, timestamp: Timestamp)
final case class Pair(from: Currency, to: Currency)
