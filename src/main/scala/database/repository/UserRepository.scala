package database.repository

import database.schema.UserTableRow
import domain.User
import zio.jdbc.{ZConnection, sqlInterpolator}
import zio.{Chunk, URIO, ZIO, ZLayer}

trait UserRepositoryAlg {
  def insertUser(user: User): ZIO[ZConnection, Throwable, Option[Long]]

  def getAllUsers: ZIO[ZConnection, Throwable, Chunk[UserTableRow]]

  def softDeleteByUserName(userName: String): ZIO[ZConnection, Throwable, Long]
}

final case class UserRepository() extends UserRepositoryAlg {
  
  override def insertUser(user: User): ZIO[ZConnection, Throwable, Option[Long]] = {
    val stmt = sql"INSERT INTO user_table(user_name, first_name, last_name, address)"
    ZIO.logInfo(stmt.toString) *> stmt.values((user.userName, user.firstName, user.lastName, user.address))
      .insertReturning[Long].map(_._2.headOption)
  }

  override def getAllUsers: ZIO[ZConnection, Throwable, Chunk[UserTableRow]] = {
    val stmt =
      sql"""
      SELECT u.id, u.user_name, u.first_name, u.last_name, u.address
      FROM user_table u
      LEFT JOIN user_delete_table ud on u .id = ud.user_id
      WHERE ud.user_id is NULL
      """
    ZIO.logInfo(stmt.toString) *> stmt.query[UserTableRow].selectAll
  }

  override def softDeleteByUserName(userName: String): ZIO[ZConnection, Throwable, Long] = {
    val stmt =
      sql"""
      INSERT INTO user_delete_table(user_id)
      SELECT u.id
      FROM user_table u
      WHERE u.user_name = $userName
      """
    ZIO.logInfo(stmt.toString) *> stmt.insert
  }
  
}

object UserRepository {
  val layer: ZLayer[Any, Nothing, UserRepositoryAlg] = ZLayer.derive[UserRepository]
}
