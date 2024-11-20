package database.repository

import zio.*
import zio.jdbc.{ZConnection, sqlInterpolator}

trait StatusRepositoryAlg {
  def select1(): ZIO[ZConnection, Throwable, Option[Int]]
}

final case class StatusRepository() extends StatusRepositoryAlg {
  override def select1(): ZIO[ZConnection, Throwable, Option[Int]] =
    ZIO.logInfo(s"Selecting 1 from database") *>
      sql"SELECT 1"
        .query[Int]
        .selectOne
}

object StatusRepository {
  val layer: ZLayer[Any, Nothing, StatusRepositoryAlg] = ZLayer.derive[StatusRepository]
}
