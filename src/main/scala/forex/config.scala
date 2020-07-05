package forex

import cats.effect.Sync
import org.http4s.Uri
import org.http4s.ParseFailure
import com.typesafe.config.ConfigException.Parse
import pureconfig.ConfigReader
import pureconfig.ConfigSource
import pureconfig.error.FailureReason
import pureconfig.error.CannotConvert
import scala.concurrent.duration.FiniteDuration

object config {

  import pureconfig.generic.auto._
  import cats.implicits._

  case class AppConfig(http: HttpConfig, oneFrameUri: Uri)
  case class HttpConfig(host: String, port: Int, timeout: FiniteDuration)

  implicit val uriReader: ConfigReader[Uri] = ConfigReader[String].emap { url =>
    Uri.fromString(url).leftMap {
      case ParseFailure(msg, _) => CannotConvert(url, "Uri", msg)
    }
  }

  /**
    * @param path the property path inside the default configuration
  **/
  def load[F[_]: Sync](path: String): F[AppConfig] =
    Sync[F].delay(ConfigSource.default.at(path).loadOrThrow[AppConfig])
}
