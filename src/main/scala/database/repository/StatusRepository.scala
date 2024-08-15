package database.repository

import domain.User
import zio.jdbc.{ZConnection, sqlInterpolator}
import zio.*

trait StatusRepositoryAlg {
  def select1(): URIO[ZConnection, Option[Int]]
}

final case class StatusRepository() extends StatusRepositoryAlg {
  override def select1(): URIO[ZConnection, Option[Int]] =
    ZIO.logInfo(s"Selecting 1 from database") *>
    sql"select 1"
      .query[Int]
      .selectOne
      .orDie
}

object StatusRepository {
  val live: ZLayer[Any, Nothing, StatusRepositoryAlg] = ZLayer.fromFunction(() => StatusRepository.apply())
}
