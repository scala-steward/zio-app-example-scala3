package database.service

import database.repository.{UserRepository, UserRepositoryAlg}
import domain.{DependenciesStatusResponse, PortDetails, StatusResponse, User}
import http.server.endpoint.HealthCheckEndpointsSpec.{suite, test}
import http.server.endpoint.{HealthCheckEndpoints, HealthCheckEndpointsAlg}
import org.testcontainers.containers
import util.generators.Generators
import zio.*
import zio.Clock.ClockLive
import zio.jdbc.{ZConnection, ZConnectionPool, ZConnectionPoolConfig}
import zio.test.*


object UserServiceSpec extends ZIOSpecDefault with Generators {

  private def mockRepo(insertResponse: URIO[ZConnection, Long]) = ZLayer.succeed(
    new UserRepositoryAlg:
      override def insertUser(user: User): URIO[ZConnection, Long] = insertResponse
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserService")(
      insertUserTests
    )

  private val insertUserTests = suite("insertUser")(
    test("calls UserRepository to persist the user - returns valid response") {
      checkN(10)(userGen) { user =>
        (for {
          insertUser <- ZIO.serviceWith[UserServiceAlg](_.insertUser)
          _ <- insertUser(user)
        } yield assertCompletes)
          .provide(
            mockRepo(ZIO.succeed(1L)),
            ZConnectionPool.h2test,
            database.service.UserService.live
          )
      }
    },
    test("calls UserRepository to persist the user - returns failure response") {
      checkN(10)(userGen) { user =>
        (for {
          insertUser <- ZIO.serviceWith[UserServiceAlg](_.insertUser)
          result <- insertUser(user).sandbox.flip
        } yield assertTrue(result.squash.getMessage == "Something went wrong with the db"))
          .provide(
            mockRepo(ZIO.dieMessage("Something went wrong with the db")),
            ZConnectionPool.h2test,
            database.service.UserService.live
          )
      }
    }
  )
}
