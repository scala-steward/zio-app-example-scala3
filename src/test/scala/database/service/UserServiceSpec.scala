package database.service

import database.repository.UserRepositoryAlg
import domain.User
import domain.error.UsernameDuplicateError
import http.server.endpoint.HealthCheckEndpointsSpec.{suite, test}
import org.postgresql.util.{PSQLException, PSQLState}
import util.generators.Generators
import zio.*
import zio.jdbc.{ZConnection, ZConnectionPool}
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
          underTest <- ZIO.service[UserServiceAlg]
          error <- underTest.insertUser(user).flip
        } yield assertTrue(
          error.isInstanceOf[UsernameDuplicateError],
          error.getMessage == "Something went wrong with the db")
          )
          .provide(
            mockRepo(ZIO.die(new PSQLException("Something went wrong with the db", PSQLState.UNIQUE_VIOLATION))),
            ZConnectionPool.h2test,
            database.service.UserService.live
          )
      }
    }
  )
}
