package util

import com.dimafeng.testcontainers.PostgreSQLContainer
import domain.PortDetails
import zio.*

object TestContainerResource {

  private val initAndStartPostgres: ZIO[Any, Throwable, org.testcontainers.containers.PostgreSQLContainer[?]] = for {
    container: org.testcontainers.containers.PostgreSQLContainer[?] <- ZIO.attempt(PostgreSQLContainer().container)
    _ <- ZIO.attempt(container.start())
    _ <- ZIO.logInfo(s"Starting PostgreSQL test-container instance. Url: [${container.getJdbcUrl}]" +
      s" Port: [${container.getMappedPort(PortDetails.PostgresPort.port)}]")
  } yield container

  val postgresResource: ZIO[Any & Scope, Throwable, org.testcontainers.containers.PostgreSQLContainer[?]] =
    ZIO.acquireRelease(initAndStartPostgres)(postgresSQLContainer =>
        ZIO.attempt(postgresSQLContainer.stop()).tapError( e =>
          ZIO.logError(s"Something went wrong ${e.getMessage}")
        ).orDie
      )
      .zipLeft(ZIO.logInfo("Stopping PostgreSQL server"))
      .tapError(t =>
        ZIO.logErrorCause(s"Failed to stop PostgreSQL server", Cause.die(t)))

}
