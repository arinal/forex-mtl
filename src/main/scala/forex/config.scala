package forex

import cats.effect.Sync
import pureconfig.ConfigSource
import scala.concurrent.duration.FiniteDuration

object config {

  import pureconfig.generic.auto._

  case class AppConfig(http: HttpConfig)
  case class HttpConfig(host: String, port: Int, timeout: FiniteDuration)

  /**
    * @param path the property path inside the default configuration
  **/
  def load[F[_]: Sync](path: String): F[AppConfig] =
    Sync[F].delay(ConfigSource.default.at(path).loadOrThrow[AppConfig])
}
