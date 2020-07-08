package forex.it

import weaver._
import org.http4s.client.Client
import cats.effect.Resource
import org.http4s.client.blaze.BlazeClientBuilder
import weaver.scalacheck.IOCheckers
import cats.effect.IO
import org.http4s.Status

object ForexTest extends IOSuite with IOCheckers {

  override type Res = Client[IO]
  override def sharedResource: Resource[IO, Res] =
    BlazeClientBuilder[IO](ec).resource

  test("'GET /rates' returns Ok") { client =>
    for {
      code <- client.get("http://localhost:9090/rates") { resp =>
        IO.pure(resp.status)
      }
    } yield expect(code == Status.Ok)
  }

  test("'GET /rates?(invalid pair)' returns BadRequest") { client =>
    for {
      code <- client.get("http://localhost:9090/rates?from=IDR&to=IDR") { resp =>
        IO.pure(resp.status)
      }
    } yield expect(code == Status.BadRequest)
  }
}
