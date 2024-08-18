package http.server.endpoint

import domain.User
import domain.error.*
import domain.payload.CreateUserPayload
import domain.response.SuccessfulResponse
import program.UserProgramAlg
import util.generators.Generators
import zio.*
import zio.http.*
import zio.jdbc.ZConnectionPool
import zio.json.*
import zio.test.*


object UserEndpointsSpec extends ZIOSpecDefault with Generators {

  private def userProgramMock(response: ZIO[Any, ServiceError, Unit]): ULayer[UserProgramAlg] =
    ZLayer.succeed(
      new UserProgramAlg {
        override def insertUser(user: User): ZIO[Any, ServiceError, Unit] = response
      }
    )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserEndpoints")(
      insertUserEndpointTests
    )

  private val insertUserEndpointTests = suite("post /user")(
    test("returns 201 when provided the proper payload and is successfully inserted") {
      checkN(1)(createUserPayload) { createUserPayload =>
        for {
          routes <- ZIO.serviceWith[UserEndpointsAlg](_.routes)
          url <- ZIO.fromEither(URL.decode("/user"))
          validStatusRequest = Request(
            method = Method.POST,
            url = url,
            body = Body.fromString(createUserPayload.toJson)
          )
          response <- routes.runZIO(validStatusRequest)
          body <- response.body.asString
          expected = SuccessfulResponse("success")
        } yield assertTrue(
          response.status == Status.Created,
          body == expected.toJson
        )
      }

    }.provide(
      userProgramMock(ZIO.unit),
      UserEndpoints.live,
      ZConnectionPool.h2test
    ),
    test("returns 500 when the program fails") {
      checkN(1)(createUserPayload) { createUserPayload =>
        for {
          routes <- ZIO.serviceWith[UserEndpointsAlg](_.routes)
          url <- ZIO.fromEither(URL.decode("/user"))
          validStatusRequest = Request(
            method = Method.POST,
            url = url,
            body = Body.fromString(createUserPayload.toJson)
          )
          response <- routes.runZIO(validStatusRequest)
          body <- response.body.asString
        } yield assertTrue(
          response.status == Status.InternalServerError
        )
      }
    }.provide(
      ZConnectionPool.h2test,
      userProgramMock(ZIO.dieMessage("Program error")),
      UserEndpoints.live
    )
  )

}
