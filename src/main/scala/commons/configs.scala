package commons

import org.http4s.Uri
import org.http4s.ParseFailure
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import scala.util.Try

object configs {

  import cats.implicits._

  implicit val uriReader: ConfigReader[Uri] = ConfigReader[String].emap { url =>
    Uri.fromString(url).leftMap {
      case ParseFailure(msg, _) => CannotConvert(url, "Uri", msg)
    }
  }
}
