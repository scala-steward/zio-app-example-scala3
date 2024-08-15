package http.server.endpoint

import database.service.{StatusService, StatusServiceAlg}
import domain.{DependenciesStatusResponse, StatusResponse}
import program.{HealthProgram, HealthProgramAlg}
import zio.*
import zio.http.*
import zio.jdbc.ZConnectionPool
import zio.json.*
import zio.test.*


object HealthCheckEndpointsSpec extends ZIOSpecDefault {

  private def healthProgramMock(getStatusesResponse: ZIO[ZConnectionPool, Throwable, Map[String, String]]): ULayer[HealthProgramAlg] =
    ZLayer.succeed(
      new HealthProgramAlg {
        override def getStatuses: ZIO[ZConnectionPool, Throwable, Map[String, String]] = getStatusesResponse
      }
    )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("HealthCheckEndpoints")(
      statusEndpointTests,
      statusDependenciesEndpointTests
    )

  private val statusEndpointTests = suite("/status")(
    test("returns 200 and a proper json body when GET /status is called") {
      (for {
        routes <- ZIO.serviceWith[HealthCheckEndpointsAlg](_.routes)
        url <- ZIO.fromEither(URL.decode("/status"))
        validStatusRequest = Request(
          method = Method.GET,
          url = url
        )
        response <- routes.runZIO(validStatusRequest)
        body <- response.body.asString
        expected = StatusResponse("Ok")
      } yield assertTrue(
        response.status == Status.Ok,
        body == expected.toJson
      ))
    }.provide(
      ZConnectionPool.h2test,
      healthProgramMock(ZIO.succeed(Map.empty)),
      HealthCheckEndpoints.live
    )
  )

  private val statusDependenciesEndpointTests = suite("/status/dependencies")(
    test("returns 200 with a dependencies map") {
      for {
        routes <- ZIO.serviceWith[HealthCheckEndpointsAlg](_.routes)
        url <- ZIO.fromEither(URL.decode("/status/dependencies"))
        validStatusRequest = Request(
          method = Method.GET,
          url = url
        )
        expected = DependenciesStatusResponse(Map("dependency" -> "Ok"))
        response <- routes.runZIO(validStatusRequest)
        body <- response.body.asString
      } yield assertTrue(
        response.status == Status.Ok,
        body == expected.toJson
      )
    }
  ).provide(
    ZConnectionPool.h2test,
    healthProgramMock(ZIO.succeed(Map(
      "dependency" -> "Ok"
    ))),
    HealthCheckEndpoints.live,
  )

}
