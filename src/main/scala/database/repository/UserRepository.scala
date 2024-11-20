package database.repository

import database.schema.UserTableRow
import domain.User
import zio.jdbc.{ZConnection, sqlInterpolator}
import zio.{Chunk, URIO, ZIO, ZLayer}

trait UserRepositoryAlg {
  def insertUser(user: User): ZIO[ZConnection, Throwable, Long]

  def getAllUsers: ZIO[ZConnection, Nothing, Chunk[UserTableRow]]

  def deleteUserByUsername(userName: String): ZIO[ZConnection, Throwable, Long]
}

final case class UserRepository() extends UserRepositoryAlg {
  override def insertUser(user: User): ZIO[ZConnection, Throwable, Long] = ZIO.logInfo("Inserting into user_table") *>
    sql"INSERT INTO user_table (user_name, first_name, last_name, address)"
      .values((user.userName, user.firstName, user.lastName, user.address))
      .insert

  override def getAllUsers: ZIO[ZConnection, Nothing, Chunk[UserTableRow]] = ZIO.logInfo("Retrieving all users from the user_table") *>
    sql"SELECT id, user_name, first_name, last_name, address FROM user_table".query[UserTableRow].selectAll.orDie

  override def deleteUserByUsername(userName: String): ZIO[ZConnection, Throwable, Long] =
    ZIO.logInfo(s"Deleting user by $userName") *> sql"DELETE FROM user_table WHERE user_name=$userName"
      .delete
}

object UserRepository {
  val layer: ZLayer[Any, Nothing, UserRepositoryAlg] = ZLayer.derive[UserRepository]
}
