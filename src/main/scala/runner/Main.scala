package runner

import zio.http.*
import zio.*
import _root_.http.server.endpoint.*
import database.migration.FlywayResource
import database.repository.StatusRepository
import database.service.StatusService
import database.util.ZConnectionPoolWrapper
import domain.PortDetails
import program.HealthProgram
import zio.http.endpoint.openapi.*
import zio.http.codec.PathCodec
import zio.jdbc.{ZConnectionPool, ZConnectionPoolConfig}


object Main extends ZIOAppDefault {

  import PathCodec.*

  private val logLevel: Status => LogLevel = (incomingStatus: Status) =>
    if (incomingStatus.isSuccess) LogLevel.Info
    else LogLevel.Error

  override def run: ZIO[Any, Throwable, Unit] = {
    val url = "jdbc:postgresql://localhost:5432/zio-app-example"

    (for {
        _ <- FlywayResource.flywayResource(url, "postgres", "postgres")
        (healthEndpoints, healthRoutes) <- ZIO.serviceWith[HealthCheckEndpointsAlg] { service => (service.endpoints, service.routes) }
        loggingMiddleware = Middleware.requestLogging(
          logRequestBody = true,
          logResponseBody = true,
          level = logLevel
        )
        composedEndpoints = healthEndpoints // todo: add new endpoints here
        openAPI = OpenAPIGen.fromEndpoints(title = "ZIO Http Swagger Example", version = "1.0", composedEndpoints)
        swaggerRoute = SwaggerUI.routes("docs" / "openapi", openAPI)
        composedRoutesWithLogging = (healthRoutes ++ swaggerRoute) @@ loggingMiddleware //todo: add new routes here
        _ <- Server.serve(composedRoutesWithLogging)
      } yield ()).provide(
        Scope.default,
        ZLayer.succeed(ZConnectionPoolConfig.default),
        ZConnectionPoolWrapper.connectionPool(
          host = "localhost",
          port = PortDetails.PostgresPort.port,
          database = "zio-app-example",
          user = "postgres",
          password = "postgres"
        ).orDie,
        StatusRepository.live,
        StatusService.live,
        HealthProgram.live,
        HealthCheckEndpoints.live,
        Server.default.orDie
      )
  }
}
