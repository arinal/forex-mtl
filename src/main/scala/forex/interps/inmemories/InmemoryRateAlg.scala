package forex.interps.inmemories

import forex.core.rates
import rates.domains._
import rates.errors

import cats.data.NonEmptyList
import cats.effect.concurrent.Ref
import cats.implicits._
import cats.effect.Sync
import scala.concurrent.duration.FiniteDuration

class InmemoryRateAlg[F[_]: Sync](mapRef: Ref[F, Map[Pair, Rate]]) extends rates.Algebra[F] {

  override def get(pairs: NonEmptyList[Pair]): F[errors.Error Either NonEmptyList[Rate]] =
    getByPairs(pairs)

  override def get(pair: Pair): F[errors.Error Either Rate] =
    getByPairs(NonEmptyList.one(pair)).map {
      case Right(rates) => rates.head.asRight
      case Left(err)    => err.asLeft
    }

  private def getByPairs(pairs: NonEmptyList[Pair]): F[errors.Error Either NonEmptyList[Rate]] =
    for {
      rateMap  <- mapRef.get
      rateList = pairs.toList.flatMap(p => rateMap.get(p))
      ratesOpt = NonEmptyList.fromList(rateList)
      result = Either.fromOption(
        ratesOpt,
        errors.Error.RateLookupFailed("Currency not found in cache")
      )
    } yield result
}

object InmemoryRateAlg {

  /**
   * @param mapRef a map of pair and its rate wrapped inside a pure atomic mutable reference [[Ref]]
   * @return new instance of [[InmemoryRateAlg]] wrapped in a generic effect [[F]].
  **/
  def apply[F[_]: Sync](mapRef: Ref[F, Map[Pair, Rate]]): F[InmemoryRateAlg[F]] =
    F.delay(new InmemoryRateAlg(mapRef))
}
