package program

import database.repository.StatusRepositoryAlg
import database.service.StatusServiceAlg
import zio.jdbc.{ZConnection, ZConnectionPool}
import zio.{ZIO, ZLayer}

trait HealthProgramAlg {
  def getStatuses: ZIO[ZConnectionPool, Throwable, Map[String, String]]
}

final case class HealthProgram(
                                private val statusService: StatusServiceAlg
                              ) extends HealthProgramAlg {
  override def getStatuses: ZIO[ZConnectionPool, Throwable, Map[String, String]] = {
    for {
      _ <- ZIO.logInfo("Getting statuses for dependencies")
      isDatabaseLive <- statusService.isDBLive
    } yield Map {
      "database" -> (if (isDatabaseLive) "Ok" else "Not Ok")
    }
  }
}

object HealthProgram {
  val live: ZLayer[StatusServiceAlg, Nothing, HealthProgramAlg] = ZLayer.fromFunction(
    (statusServiceAlg: StatusServiceAlg) => HealthProgram.apply(statusServiceAlg)
  )
}
