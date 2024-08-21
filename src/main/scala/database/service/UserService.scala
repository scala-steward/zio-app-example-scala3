package database.service

import database.repository.UserRepositoryAlg
import database.schema.UserTable
import domain.User
import domain.error.{ServiceError, ToDomainError, UsernameDuplicateError}
import org.postgresql.util.{PSQLException, PSQLState}
import zio.*
import zio.jdbc.{ZConnectionPool, transaction}

trait UserServiceAlg {
  def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Unit]

  def getAllUsers: ZIO[ZConnectionPool, ServiceError, Chunk[User]]
}

final case class UserService(
                              private val userRepository: UserRepositoryAlg
                            ) extends UserServiceAlg {

  override def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Unit] = transaction(
    userRepository.insertUser(user).unit
  )
    .mapErrorCause { cause =>
      cause.squash match {
        case e: PSQLException if e.getSQLState == PSQLState.UNIQUE_VIOLATION.getState =>
          Cause.fail(UsernameDuplicateError(e.getMessage))
      }
    }

  override def getAllUsers: ZIO[ZConnectionPool, ServiceError, Chunk[User]] = transaction(
    for {
      userTableChunk <- userRepository.getAllUsers
      // maybe bring in cats traverse/sequence as a dependency
      userChunk <- ZIO.foreach(
        userTableChunk.map(chunk =>
          UserTable.toDomain(chunk).mapError(t => ToDomainError(t.getMessage))
        ))(identity)
    } yield userChunk
  ).refineToOrDie[ServiceError]

}

object UserService {
  val live: ZLayer[UserRepositoryAlg, Nothing, UserServiceAlg] =
    ZLayer.fromFunction((userRepositoryAlg: UserRepositoryAlg) => UserService(userRepositoryAlg))
}