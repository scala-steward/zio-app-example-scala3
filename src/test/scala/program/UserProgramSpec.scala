package program

import database.service.{StatusServiceAlg, UserServiceAlg}
import domain.User
import domain.error.*
import util.generators.Generators
import zio.*
import zio.jdbc.ZConnectionPool
import zio.test.*

object UserProgramSpec extends ZIOSpecDefault with Generators {

  private def mockUserServiceAlg(response: ZIO[ZConnectionPool, ServiceError, Unit]) = ZLayer.succeed(
    new UserServiceAlg {
      override def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Unit] = response
    }
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserProgram")(
      insertUserTests
    )

  private val insertUserTests = suite("insertUser")(
    test("returns unit when the insertion is successful") {
      checkN(10)(userGen) { user =>
        for {
          _ <- ZIO.serviceWithZIO[UserProgramAlg](_.insertUser(user))
        } yield assertCompletes
      }
    }.provide(
      mockUserServiceAlg(ZIO.unit),
      UserProgram.live,
      ZConnectionPool.h2test
    ),
    test("returns database service error when the insertion is not successful") {
      checkN(10)(userGen) { user =>
        for {
          insertUser <- ZIO.serviceWith[UserProgramAlg](_.insertUser)
          error <- insertUser(user).flip
        } yield assertTrue(
          error == UsernameDuplicateError("username is not unique error")
        )
      }
    }.provide(
      mockUserServiceAlg(ZIO.fail(UsernameDuplicateError("username is not unique error"))),
      UserProgram.live,
      ZConnectionPool.h2test
    )
  )

}
