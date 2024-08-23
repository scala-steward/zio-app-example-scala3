package http.server.endpoint

import domain.User
import domain.error.*
import domain.payload.CreateUserPayload
import domain.response.{AllUsersResponse, SuccessfulResponse}
import program.UserProgramAlg
import util.generators.Generators
import zio.*
import zio.http.*
import zio.jdbc.ZConnectionPool
import zio.json.*
import zio.json.ast.Json
import zio.test.*


object UserEndpointsSpec extends ZIOSpecDefault with Generators {

  private def userProgramMock(
                               insertUserResponse: ZIO[Any, ServiceError, Unit],
                               getAllUsersResponse: ZIO[Any, ServiceError, Chunk[User]],
                             ): ULayer[UserProgramAlg] =
    ZLayer.succeed(
      new UserProgramAlg {
        override def insertUser(user: User): ZIO[Any, ServiceError, Unit] = insertUserResponse

        override def getAllUsers: ZIO[ZConnectionPool, ServiceError, Chunk[User]] = getAllUsersResponse
      }
    )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserEndpoints")(
      insertUserEndpointTests,
      getAllUsersEndpointTests
    )

  private val insertUserEndpointTests = suite("post /user")(
    test("returns 201 when provided the proper payload and is successfully inserted") {
      checkN(1)(createUserPayload) { createUserPayload =>
        for {
          routes <- ZIO.serviceWith[UserEndpointsAlg](_.routes)
          url <- ZIO.fromEither(URL.decode("/user"))
          request = Request(
            method = Method.POST,
            url = url,
            body = Body.fromString(createUserPayload.toJson)
          )
          response <- routes.runZIO(request)
          body <- response.body.asString
          expected = SuccessfulResponse("success")
        } yield assertTrue(
          response.status == Status.Created,
          body == expected.toJson
        )
      }
    }.provide(
      userProgramMock(
        ZIO.unit,
        ZIO.succeed(Chunk.empty)
      ),
      UserEndpoints.live,
      ZConnectionPool.h2test
    ),
    test("returns 500 when the program fibre fails") {
      checkN(1)(createUserPayload) { createUserPayload =>
        for {
          routes <- ZIO.serviceWith[UserEndpointsAlg](_.routes)
          url <- ZIO.fromEither(URL.decode("/user"))
          request = Request(
            method = Method.POST,
            url = url,
            body = Body.fromString(createUserPayload.toJson)
          )
          response <- routes.runZIO(request)
        } yield assertTrue(
          response.status == Status.InternalServerError
        )
      }
    }.provide(
      ZConnectionPool.h2test,
      userProgramMock(
        ZIO.dieMessage("Program error"),
        ZIO.succeed(Chunk.empty)
      ),
      UserEndpoints.live
    )
  )

  private val getAllUsersEndpointTests = suite("get /users")(
    test("returns 200 when a request is made to get all users and no error occurs") {
      for {
        routes <- ZIO.serviceWith[UserEndpointsAlg](_.routes)
        url <- ZIO.fromEither(URL.decode("/users"))
        request = Request(
          method = Method.GET,
          url = url
        )
        response <- routes.runZIO(request)
        body <- response.body.asString
        expected = AllUsersResponse(Chunk.succeed(User("username", "firstname", "lastname")))
      } yield assertTrue(
        response.status == Status.Ok,
        body == expected.toJson
      )
    }.provide(
      userProgramMock(
        ZIO.unit,
        ZIO.succeed(Chunk.succeed(User("username", "firstname", "lastname")))
      ),
      UserEndpoints.live,
      ZConnectionPool.h2test
    ),
    test("returns 500 when the program fails") {
      for {
        routes <- ZIO.serviceWith[UserEndpointsAlg](_.routes)
        url <- ZIO.fromEither(URL.decode("/users"))
        request = Request(
          method = Method.GET,
          url = url
        )
        response <- routes.runZIO(request)
      } yield assertTrue(
        response.status == Status.InternalServerError,
      )
    }.provide(
      ZConnectionPool.h2test,
      userProgramMock(
        ZIO.unit,
        ZIO.dieMessage("Failed to retrieve all users")
      ),
      UserEndpoints.live
    )
  )

}
