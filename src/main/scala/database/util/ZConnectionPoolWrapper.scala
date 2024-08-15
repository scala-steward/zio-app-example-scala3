package database.util

import zio.ZLayer
import zio.jdbc.*

object ZConnectionPoolWrapper {
  def connectionPool(host: String, port: Int, database: String, user: String, password: String):
  ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool] =
    ZConnectionPool.postgres(host, port, database, properties(user, password))

  private def properties(user: String, password: String) = Map(
    "user" -> user,
    "password" -> password
  )
}
