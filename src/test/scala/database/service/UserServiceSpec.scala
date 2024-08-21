package database.service

import database.repository.UserRepositoryAlg
import database.schema.UserTable
import domain.User
import domain.error.UsernameDuplicateError
import http.server.endpoint.HealthCheckEndpointsSpec.{suite, test}
import org.postgresql.util.{PSQLException, PSQLState}
import util.generators.Generators
import zio.*
import zio.jdbc.{ZConnection, ZConnectionPool}
import zio.test.*


object UserServiceSpec extends ZIOSpecDefault with Generators {

  private def mockRepo(insertResponse: URIO[ZConnection, Long], getUsersResponse: URIO[ZConnection, Chunk[UserTable]]) = ZLayer.succeed(
    new UserRepositoryAlg:
      override def insertUser(user: User): URIO[ZConnection, Long] = insertResponse

      override def getAllUsers: URIO[ZConnection, Chunk[UserTable]] = getUsersResponse
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserService")(
      insertUserTests,
      getAllUsersTest
    )

  private val insertUserTests = suite("insertUser")(
    test("calls UserRepository to persist the user - returns valid response") {
      checkN(10)(userGen) { user =>
        (for {
          insertUser <- ZIO.serviceWith[UserServiceAlg](_.insertUser)
          _ <- insertUser(user)
        } yield assertCompletes)
          .provide(
            mockRepo(
              ZIO.succeed(1L),
              ZIO.succeed(Chunk.empty)
            ),
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
            mockRepo(
              ZIO.die(new PSQLException("Something went wrong with the db", PSQLState.UNIQUE_VIOLATION)),
              ZIO.succeed(Chunk.empty)
            ),
            ZConnectionPool.h2test,
            database.service.UserService.live
          )
      }
    }
  )

  private val getAllUsersTest = suite("getAllUsers")(
    test("calls UserRepository to get all the users - returns valid response") {
      checkN(10)(chunkUserTableGen) { userTableChunk =>
        (for {
          users <- ZIO.serviceWithZIO[UserServiceAlg](_.getAllUsers)
        } yield assertTrue(
          users.length == userTableChunk.length
        ))
          .provide(
            mockRepo(
              ZIO.succeed(1L),
              ZIO.succeed(userTableChunk)
            ),
            ZConnectionPool.h2test,
            database.service.UserService.live
          )
      }
    },
    test("calls UserRepository to get all the persisted users - returns failure response") {
      (for {
        error <- ZIO.serviceWithZIO[UserServiceAlg](_.getAllUsers).sandbox.flip
      } yield assertTrue(
        error.squash.getMessage == "Something went wrong whilst getting all the users")
        )
        .provide(
          mockRepo(
            ZIO.succeed(1L),
            ZIO.dieMessage("Something went wrong whilst getting all the users")
          ),
          ZConnectionPool.h2test,
          database.service.UserService.live
        )
    }
  )
}
