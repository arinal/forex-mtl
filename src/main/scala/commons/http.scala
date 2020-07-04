package commons

import org.http4s.{ EntityDecoder, EntityEncoder }
import io.circe.{ Decoder, Encoder }
import cats.effect.Sync

package object http {

  import org.http4s.circe._

  implicit def jsonDecoder[A <: Product: Decoder, F[_]: Sync]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def jsonEncoder[A <: Product: Encoder, F[_]: Sync]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
}
