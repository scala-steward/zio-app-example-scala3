package database.repository

import zio.*
import zio.jdbc.{ZConnection, sqlInterpolator}

trait StatusRepositoryAlg {
  def select1(): ZIO[ZConnection, Throwable, Option[Int]]
}

final case class StatusRepository() extends StatusRepositoryAlg {
  override def select1(): ZIO[ZConnection, Throwable, Option[Int]] =
    val stmt = sql"SELECT 1"
    ZIO.logInfo(stmt.toString) *> stmt.query[Int].selectOne
}

object StatusRepository {
  val layer: ZLayer[Any, Nothing, StatusRepositoryAlg] = ZLayer.derive[StatusRepository]
}
