package database.service

import zio.{ZIO, ZLayer}
import database.repository.UserRepositoryAlg
import domain.User
import zio.jdbc.{ZConnectionPool, transaction}

trait UserServiceAlg {
  def insertUser(user: User): ZIO[ZConnectionPool, Throwable, Unit]
}

final case class UserService(
                              private val userRepository: UserRepositoryAlg
                            ) extends UserServiceAlg {
  
  override def insertUser(user: User): ZIO[ZConnectionPool, Throwable, Unit] = transaction(
    userRepository.insertUser(user).unit
  )

}

object UserService {
  val live: ZLayer[UserRepositoryAlg, Nothing, UserServiceAlg] =
    ZLayer.fromFunction((userRepositoryAlg: UserRepositoryAlg) => UserService(userRepositoryAlg))
}