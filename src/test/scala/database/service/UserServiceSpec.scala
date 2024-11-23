package database.service

import database.repository.UserRepositoryAlg
import database.schema.UserTableRow
import domain.User
import domain.error.{DatabaseTransactionError, UserAlreadyDeletedError, UserNotInsertedError, UsernameDuplicateError}
import http.server.endpoint.HealthCheckEndpointsSpec.{suite, test}
import org.postgresql.util.{PSQLException, PSQLState}
import util.generators.Generators
import zio.*
import zio.jdbc.{ZConnection, ZConnectionPool}
import zio.test.*


object UserServiceSpec extends ZIOSpecDefault with Generators {

  private def mockUserRepo(
                            insertResponse: ZIO[ZConnection, Throwable, Option[Long]],
                            getUsersResponse: ZIO[ZConnection, Throwable, Chunk[UserTableRow]],
                            deleteUserResponse: ZIO[ZConnection, Throwable, Long]
                          ) = ZLayer.succeed(
    new UserRepositoryAlg:
      override def insertUser(user: User): ZIO[ZConnection, Throwable, Option[Long]] = insertResponse

      override def getAllUsers: ZIO[ZConnection, Throwable, Chunk[UserTableRow]] = getUsersResponse

      override def softDeleteByUserName(userName: String): ZIO[ZConnection, Throwable, Long] = deleteUserResponse
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserService")(
      insertUserTests,
      getAllUsersTest,
      deleteUserByUsernameTest
    )

  private val insertUserTests = suite("insertUser")(
    test("calls UserRepository to persist the user - returns valid response") {
      checkN(10)(userGen) { user =>
        (for {
          insertUser <- ZIO.serviceWith[UserServiceAlg](_.insertUser)
          userId <- insertUser(user)
        } yield assertTrue(
          userId == 1L
        ))
          .provide(
            mockUserRepo(
              insertResponse = ZIO.some(1L),
              getUsersResponse = ZIO.succeed(Chunk.empty),
              deleteUserResponse = ZIO.succeed(0L)
            ),
            ZConnectionPool.h2test,
            database.service.UserService.layer
          )
      }
    },
    test("calls UserRepository to persist the user - returns error when no userid is returned") {
      checkN(10)(userGen) { user =>
        (for {
          insertUser <- ZIO.serviceWith[UserServiceAlg](_.insertUser)
          error <- insertUser(user).flip
        } yield assertTrue(
          error == UserNotInsertedError("Insert did not return a user id")
        ))
          .provide(
            mockUserRepo(
              insertResponse = ZIO.none,
              getUsersResponse = ZIO.succeed(Chunk.empty),
              deleteUserResponse = ZIO.succeed(0L)
            ),
            ZConnectionPool.h2test,
            database.service.UserService.layer
          )
      }
    },
    test("calls UserRepository to persist the user - returns failure when there is a unique constraint error") {
      checkN(10)(userGen) { user =>
        (for {
          underTest <- ZIO.service[UserServiceAlg]
          error <- underTest.insertUser(user).flip
        } yield assertTrue(
          error == UsernameDuplicateError("username already found")
        ))
          .provide(
            mockUserRepo(
              insertResponse = ZIO.die(PSQLException("username already found", PSQLState.UNIQUE_VIOLATION)),
              getUsersResponse = ZIO.succeed(Chunk.empty),
              deleteUserResponse = ZIO.succeed(0L)
            ),
            ZConnectionPool.h2test,
            database.service.UserService.layer
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
            mockUserRepo(
              insertResponse = ZIO.some(1L),
              getUsersResponse = ZIO.succeed(userTableChunk),
              deleteUserResponse = ZIO.succeed(0L)
            ),
            ZConnectionPool.h2test,
            database.service.UserService.layer
          )
      }
    },
    test("calls UserRepository to get all the persisted users - returns failure response when the effect dies") {
      (for {
        error <- ZIO.serviceWithZIO[UserServiceAlg](_.getAllUsers).flip
      } yield assertTrue(
        error == DatabaseTransactionError("Something went wrong whilst getting all the users")
      ))
        .provide(
          mockUserRepo(
            insertResponse = ZIO.some(1L),
            getUsersResponse = ZIO.dieMessage("Something went wrong whilst getting all the users"),
            deleteUserResponse = ZIO.succeed(0L)
          ),
          ZConnectionPool.h2test,
          database.service.UserService.layer
        )
    }
  )

  private val deleteUserByUsernameTest = suite("deleteUserByUsername")(
    test("calls UserRepository to delete a user by username - returns valid response") {
      (for {
        _ <- ZIO.serviceWithZIO[UserServiceAlg](_.deleteUserByUsername("username"))
      } yield assertCompletes)
        .provide(
          mockUserRepo(
            insertResponse = ZIO.some(0L),
            getUsersResponse = ZIO.succeed(Chunk.empty),
            deleteUserResponse = ZIO.succeed(1L)
          ),
          ZConnectionPool.h2test,
          database.service.UserService.layer
        )
    },
    test("calls UserRepository to delete a user by username - returns failure response when the effect dies") {
      (for {
        error <- ZIO.serviceWithZIO[UserServiceAlg](_.deleteUserByUsername("username")).flip
      } yield assertTrue(
        error == DatabaseTransactionError("Something went wrong whilst deleting user")
      ))
        .provide(
          mockUserRepo(
            insertResponse = ZIO.some(1L),
            getUsersResponse = ZIO.succeed(Chunk.empty),
            deleteUserResponse = ZIO.dieMessage("Something went wrong whilst deleting user"),
          ),
          ZConnectionPool.h2test,
          database.service.UserService.layer
        )
    },
    test("calls UserRepository to delete a user by username - returns failure when the user has already been deleted") {
      (for {
        error <- ZIO.serviceWithZIO[UserServiceAlg](_.deleteUserByUsername("username")).flip
      } yield assertTrue(
        error == UserAlreadyDeletedError("user already deleted")
      ))
        .provide(
          mockUserRepo(
            insertResponse = ZIO.some(1L),
            getUsersResponse = ZIO.succeed(Chunk.empty),
            deleteUserResponse = ZIO.fail(PSQLException("user already deleted", PSQLState.UNIQUE_VIOLATION))
          ),
          ZConnectionPool.h2test,
          database.service.UserService.layer
        )
    }
  )
}
