package forex

import config.AppConfig
import app.http.RatesRoutes
import interps.dummy.OneFrameDummyRate

import org.http4s.HttpRoutes
import org.http4s.HttpApp
import cats.effect.Sync
import cats.effect.Timer

class Module[F[_]](val routes: HttpApp[F])

object Module {

  import cats.implicits._
  import org.http4s._
  import org.http4s.implicits._

  def apply[F[_]: Sync: Timer](config: AppConfig) =
    for {
      rateAlg <- OneFrameDummyRate[F]
      program = app.programs.rates.Algebra[F](rateAlg)
      routes = new RatesRoutes(program).routes.orNotFound
    } yield new Module(routes)
}
