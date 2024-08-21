package runner

import zio.http.*
import zio.*
import _root_.http.server.endpoint.*
import database.migration.FlywayResource
import database.repository.{StatusRepository, UserRepository}
import database.service.{StatusService, UserService}
import database.util.ZConnectionPoolWrapper
import program.{HealthProgram, UserProgram}
import zio.http.endpoint.openapi.*
import zio.http.codec.PathCodec
import zio.jdbc.{ZConnectionPool, ZConnectionPoolConfig}
import configuration.{ApplicationConfig, ApplicationConfigAlg}
import zio.config.typesafe.TypesafeConfigProvider
import PathCodec.*

object Main extends ZIOAppDefault {


  private val logLevel: Status => LogLevel = (incomingStatus: Status) =>
    if (incomingStatus.isSuccess) LogLevel.Info
    else LogLevel.Error

  override def run: ZIO[Any, Throwable, Unit] =
    ZIO.serviceWithZIO[ApplicationConfigAlg](_.hoconConfig).flatMap { (appConfig: ApplicationConfig.HoconConfig) =>
      (for {
        _ <- FlywayResource.flywayResource(appConfig.db.jdbcUrl, appConfig.db.user, appConfig.db.password)
        (healthEndpoints, healthRoutes) <- ZIO.serviceWith[HealthCheckEndpointsAlg] { service => (service.endpoints, service.routes) }
        (userEndpoints, userRoutes) <- ZIO.serviceWith[UserEndpointsAlg] { service => (service.endpoints, service.routes) }
        loggingMiddleware = Middleware.requestLogging(
          logRequestBody = true,
          logResponseBody = true,
          level = logLevel
        )
        composedEndpoints = healthEndpoints ++ userEndpoints // todo: add new endpoints here
        openAPI = OpenAPIGen.fromEndpoints(title = "ZIO application example", version = "1.0", composedEndpoints)
        swaggerRoute = SwaggerUI.routes("docs" / "openapi", openAPI)
        composedRoutesWithLogging = (healthRoutes ++ userRoutes ++ swaggerRoute) @@ loggingMiddleware //todo: add new routes here
        _ <- Server.serve(composedRoutesWithLogging)
      } yield ()).provide(
        Scope.default,
        ZLayer.succeed(ZConnectionPoolConfig.default),
        ZConnectionPoolWrapper.connectionPool(
          host = appConfig.db.host,
          port = appConfig.db.exposedPort,
          database = appConfig.db.database,
          user = appConfig.db.user,
          password = appConfig.db.password
        ).orDie,
        StatusRepository.live,
        UserRepository.live,
        StatusService.live,
        UserService.live,
        HealthProgram.live,
        UserProgram.live,
        HealthCheckEndpoints.live,
        UserEndpoints.live,
        Server.default.orDie
      )
    }.provide(
      ApplicationConfig.live,
      ZLayer.succeed(TypesafeConfigProvider.fromResourcePath())
    )
}
