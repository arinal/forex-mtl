package commons

import org.http4s.{ EntityDecoder, EntityEncoder }
import io.circe.{ Decoder, Encoder }
import cats.effect.Sync
import cats.Applicative

package object http {

  import org.http4s.circe._

  implicit def deriveEntityEncoder[F[_]: Applicative, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
  implicit def deriveEntityDecoder[F[_]: Sync, A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
}
