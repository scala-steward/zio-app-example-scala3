package util

import org.flywaydb.core.Flyway
import zio.*

object FlywayResource {

  private def initFlyway(url: String, user: String, password: String): Task[Flyway] = ZIO.attempt {
    val flyway = Flyway.configure()
      .cleanDisabled(false)
      .dataSource(url, user, password).load()
    flyway.migrate()
    flyway
  } <* ZIO.logInfo(s"Setting up Flyway migrations with the following: url [$url] user [$user] password [$password]")

  def flywayResource(url: String, user: String, password: String): ZIO[Any & Scope, Throwable, Flyway] =
    ZIO.acquireRelease(initFlyway(url, user, password))(flyway =>
        ZIO.attempt(flyway.clean()).orDie
      )
      .zipLeft(ZIO.logInfo("Cleaning Flyway migrations"))
      .tapError(t =>
        ZIO.logErrorCause(s"Error whilst cleaning migrations", Cause.die(t))
      )

}
