package database.repository

import database.schema.UserTable
import domain.User
import zio.jdbc.{ZConnection, sqlInterpolator}
import zio.{Chunk, URIO, ZIO, ZLayer}

trait UserRepositoryAlg {
  def insertUser(user: User): URIO[ZConnection, Long]
  def getAllUsers: URIO[ZConnection, Chunk[UserTable]]
}

final case class UserRepository() extends UserRepositoryAlg {
  override def insertUser(user: User): URIO[ZConnection, Long] = ZIO.logInfo("Inserting into user_table") *>
      sql"insert into user_table (user_name, first_name, last_name)"
        .values((user.userName, user.firstName, user.lastName))
        .insert
        .orDie

  override def getAllUsers: URIO[ZConnection, Chunk[UserTable]] = ZIO.logInfo("Retrieving all users from the user_table") *>
    sql"select * from user_table".query[UserTable].selectAll.orDie
}

object UserRepository {
  val live: ZLayer[Any, Nothing, UserRepositoryAlg] = ZLayer.fromFunction(() => UserRepository.apply())
}
