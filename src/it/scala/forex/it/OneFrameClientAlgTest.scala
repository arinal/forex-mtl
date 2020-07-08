package forex
package it

import interps.http.PaidyOneFrameRateClientAlg
import core.rates.domains.Currency
import core.rates.domains.Pair
import core.rates.domains.Rate
import core.rates.errors

import weaver._
import weaver.scalacheck.IOCheckers
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.Uri
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import cats.effect.{IO, Resource}

object OneFrameClientAlgTest extends IOSuite with IOCheckers {

  import cats.implicits._
  import Rate._

  val uri             = Uri.fromString("http://localhost:8080").right.get
  implicit val logger = Slf4jLogger.getLogger[IO]

  override type Res = PaidyOneFrameRateClientAlg[IO]
  override def sharedResource: Resource[IO, Res] =
    BlazeClientBuilder[IO](ec).resource.evalMap { client =>
      PaidyOneFrameRateClientAlg(uri, client, "10dc303535874aeccc86a8251e6992f5")
    }

  test("get valid pair should get right result") { algebra =>
    import forex.it.arbiters.currencies.validPairs
    forall { pair: Pair =>
      algebra.get(pair).map(either => expect(either.isRight))
    }
  }

  test("get invalid double pair get double pair error result") { algebra =>
    import forex.it.arbiters.currencies.invalidPairs
    forall { pair: Pair =>
      algebra.get(pair).map(either => expect(either == Left(errors.Error.DoublePair)))
    }
  }
}
