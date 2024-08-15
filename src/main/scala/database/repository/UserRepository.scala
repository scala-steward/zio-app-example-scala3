package database.repository

import domain.User
import zio.jdbc.{ZConnection, sqlInterpolator}
import zio.{URIO, ZIO, ZLayer}

trait UserRepositoryAlg {
  def insertUser(user: User): URIO[ZConnection, Long]
}

final case class UserRepository() extends UserRepositoryAlg {
  override def insertUser(user: User): URIO[ZConnection, Long] =
    ZIO.logInfo(s"Inserting into user_table") *>
      sql"insert into user_table (user_name, first_name, last_name)"
        .values((user.userName, user.firstName, user.lastName))
        .insert
        .orDie
}

object UserRepository {
  val live: ZLayer[Any, Nothing, UserRepositoryAlg] = ZLayer.fromFunction(() => UserRepository.apply())
}
