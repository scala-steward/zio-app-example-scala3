package database.migration

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import zio.*

object FlywayResource {

  private def initFlyway(url: String, user: String, password: String): Task[Unit] = ZIO.attempt {
    val flyway = Flyway.configure()
      .cleanDisabled(true)
      .dataSource(url, user, password).load()
    flyway.migrate()
  }.unit <* ZIO.logInfo(s"Setting up Flyway migrations with the following: url [$url] user [$user] password [$password]")

  def flywayResource(url: String, user: String, password: String): ZIO[Any & Scope, Throwable, Unit] =
    ZIO.acquireRelease(initFlyway(url, user, password))(_ =>
        ZIO.logInfo("not cleaning migrations")
      )
      .tapErrorCause(t =>
        ZIO.logErrorCause(s"Error whilst cleaning migrations", t)
      )

}
