package forex.app.http

import protocols.RateResponse
import forex.core.rates.domains.Rate

object converters {

  implicit class RateResponseOps(val rate: Rate) extends AnyVal {
    def toResponse: RateResponse =
      RateResponse(from = rate.pair.from, to = rate.pair.to, price = rate.price, timestamp = rate.timestamp)
  }
}
