package database.service

import database.repository.UserRepositoryAlg
import domain.User
import domain.error.{ServiceError, UsernameDuplicateError}
import org.postgresql.util.{PSQLException, PSQLState}
import zio.*
import zio.jdbc.{ZConnectionPool, transaction}

trait UserServiceAlg {
  def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Unit]
}

final case class UserService(
                              private val userRepository: UserRepositoryAlg
                            ) extends UserServiceAlg {

  override def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Unit] = transaction(
    userRepository.insertUser(user).unit
  )
    .mapErrorCause{ cause =>
      cause.squash match {
        case e: PSQLException if e.getSQLState == PSQLState.UNIQUE_VIOLATION.getState =>
          Cause.fail(UsernameDuplicateError(e.getMessage))
      }
    }

}

object UserService {
  val live: ZLayer[UserRepositoryAlg, Nothing, UserServiceAlg] =
    ZLayer.fromFunction((userRepositoryAlg: UserRepositoryAlg) => UserService(userRepositoryAlg))
}