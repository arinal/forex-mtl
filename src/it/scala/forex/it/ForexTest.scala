package forex
package it

import core.rates.domains.Pair
import core.rates.domains.Rate

import weaver._
import org.http4s.client.Client
import cats.effect.Resource
import org.http4s.client.blaze.BlazeClientBuilder
import weaver.scalacheck.IOCheckers
import cats.effect.IO
import org.http4s.Status

object ForexTest extends IOSuite with IOCheckers {

  import Rate._

  test("'GET /rates?(valid pair)' get single rate returns Ok") { client =>
    import forex.it.arbiters.currencies.validPairs
    forall { pair: Pair =>
      for {
        code <- client.get(singleRateUrl(pair)) { resp =>
                  IO.pure(resp.status)
                }
      } yield expect(code == Status.Ok)
    }
  }

  test("'GET /rates' : get all rates returns Ok") { client =>
    for {
      code <- client.get("http://localhost:9090/rates") { resp =>
                IO.pure(resp.status)
              }
    } yield expect(code == Status.Ok)
  }

  test("'GET /rates?(invalid pair)' get single rate with invalid pair returns BadRequest") {
    client =>
      import forex.it.arbiters.currencies.invalidPairs
      forall { pair: Pair =>
        for {
          code <- client.get(singleRateUrl(pair)) { resp =>
                    IO.pure(resp.status)
                  }
        } yield expect(code == Status.BadRequest)
      }
  }

  override type Res = Client[IO]
  override def sharedResource: Resource[IO, Res] =
    BlazeClientBuilder[IO](ec).resource

  def singleRateUrl(pair: Pair) =
    s"http://localhost:9090/rates?from=${pair.from}&to=${pair.to}"
}
