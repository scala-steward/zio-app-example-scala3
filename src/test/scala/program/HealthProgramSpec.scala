package program

import database.service.StatusServiceAlg
import domain.{DependenciesStatusResponse, StatusResponse}
import zio.*
import zio.http.*
import zio.jdbc.ZConnectionPool
import zio.json.*
import zio.test.*

object HealthProgramSpec extends ZIOSpecDefault {

  private def mockStatusServiceAlg(response: ZIO[ZConnectionPool, Throwable, Boolean]) = ZLayer.succeed(
    new StatusServiceAlg {
      override def isDBLive: ZIO[ZConnectionPool, Throwable, Boolean] = response
    }
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("HealthProgram")(
      getStatuses
    )

  private val getStatuses = suite("getStatuses")(
    test("returns a map with the dependencies statuses - returns Ok when the status service returns true") {
      for {
        statuses <- ZIO.serviceWithZIO[HealthProgramAlg](_.getStatuses)
        expected = Map[String, String]("database" -> "Ok")
      } yield assertTrue(
        statuses == expected
      )
    }.provide(
      mockStatusServiceAlg(ZIO.succeed(true)),
      HealthProgram.live,
      ZConnectionPool.h2test
    ),
    test("returns a map with the dependencies statuses - returns Not Ok when the status service returns trye") {
      for {
        statuses <- ZIO.serviceWithZIO[HealthProgramAlg](_.getStatuses)
        expected = Map[String, String]("database" -> "Not Ok")
      } yield assertTrue(
        statuses == expected
      )
    }.provide(
      mockStatusServiceAlg(ZIO.succeed(false)),
      HealthProgram.live,
      ZConnectionPool.h2test
    )
  )

}
