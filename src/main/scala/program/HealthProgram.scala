package program

import database.service.StatusServiceAlg
import zio.jdbc.ZConnectionPool
import zio.{ZIO, ZLayer}

trait HealthProgramAlg {
  def getStatuses: ZIO[ZConnectionPool, Throwable, Map[String, String]]
}

final case class HealthProgram(
                                private val statusService: StatusServiceAlg
                              ) extends HealthProgramAlg {
  override def getStatuses: ZIO[ZConnectionPool, Throwable, Map[String, String]] =
    for {
      _ <- ZIO.logInfo("Getting statuses for dependencies")
      isDatabaseLive <- statusService.isDBLive
    } yield Map {
      "database" -> (if (isDatabaseLive) "Ok" else "Not Ok")
    }
}

object HealthProgram {
  val layer: ZLayer[StatusServiceAlg, Nothing, HealthProgramAlg] = ZLayer.derive[HealthProgram]
}
