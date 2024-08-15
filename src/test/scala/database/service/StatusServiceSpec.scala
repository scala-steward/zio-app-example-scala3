package database.service

import database.repository.{StatusRepository, StatusRepositoryAlg}
import domain.{DependenciesStatusResponse, PortDetails, StatusResponse, User}
import http.server.endpoint.HealthCheckEndpointsSpec.{suite, test}
import http.server.endpoint.{HealthCheckEndpoints, HealthCheckEndpointsAlg}
import org.testcontainers.containers
import util.generators.Generators
import zio.*
import zio.Clock.ClockLive
import zio.jdbc.{ZConnection, ZConnectionPool, ZConnectionPoolConfig}
import zio.test.*


object StatusServiceSpec extends ZIOSpecDefault with Generators {

  private def mockRepo(select1Resposnse: URIO[ZConnection, Option[Int]]) = ZLayer.succeed(
    new StatusRepositoryAlg:
      override def select1(): URIO[ZConnection, Option[Int]] = select1Resposnse
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserService")(
      select1Tests
    )

  private val select1Tests = suite("select1")(
    test("returns true when the repository responds with Some(1)") {
      (for {
        isDBLive <- ZIO.serviceWithZIO[StatusServiceAlg](_.isDBLive)
      } yield assertTrue(isDBLive))
        .provide(
          mockRepo(ZIO.succeed(Option(1))),
          ZConnectionPool.h2test,
          ZLayer.succeed(ZConnectionPoolConfig.default),
          StatusService.live
        )
    },
    test("returns false when the repository responds with None") {
      (for {
        isDBLive <- ZIO.serviceWithZIO[StatusServiceAlg](_.isDBLive)
      } yield assertTrue(!isDBLive))
        .provide(
          mockRepo(ZIO.none),
          ZConnectionPool.h2test,
          ZLayer.succeed(ZConnectionPoolConfig.default),
          StatusService.live
        )
    }
  )
}
