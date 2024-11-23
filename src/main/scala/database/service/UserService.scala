package database.service

import database.repository.UserRepositoryAlg
import database.schema.UserTableRow
import domain.User
import domain.error.*
import org.postgresql.util.{PSQLException, PSQLState}
import zio.*
import zio.jdbc.{ZConnectionPool, transaction}

trait UserServiceAlg {
  def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Long]

  def getAllUsers: ZIO[ZConnectionPool, ServiceError, Chunk[User]]

  def deleteUserByUsername(userName: String): ZIO[ZConnectionPool, ServiceError, Unit]
}

final case class UserService(
                              private val userRepository: UserRepositoryAlg
                            ) extends UserServiceAlg {

  override def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Long] = transaction(
    userRepository.insertUser(user).flatMap {
      case Some(value) => ZIO.succeed(value)
      case None => ZIO.fail(UserNotInsertedError("Insert did not return a user id"))
    }
  ).mapErrorCause { cause =>
      cause.squash match {
        case e: PSQLException if e.getSQLState == PSQLState.UNIQUE_VIOLATION.getState =>
          Cause.fail(UsernameDuplicateError(e.getMessage))
        case e: ServiceError =>
          Cause.fail(e)
        case e =>
          Cause.fail(DatabaseTransactionError(e.getMessage))
      }
    }

  override def getAllUsers: ZIO[ZConnectionPool, ServiceError, Chunk[User]] = transaction(
    for {
      userTableChunk <- userRepository.getAllUsers
      userChunk <- ZIO.foreachPar(
        userTableChunk.map(userTableRow =>
          UserTableRow.toDomain(userTableRow).mapError(t => ToDomainError(t.getMessage))
        ))(identity).withParallelism(10)
    } yield userChunk
  ).mapErrorCause { cause =>
    Cause.fail(DatabaseTransactionError(cause.squash.getMessage))
  }

  override def deleteUserByUsername(userName: String): ZIO[ZConnectionPool, ServiceError, Unit] = transaction(
    userRepository.softDeleteByUserName(userName).flatMap{
      case 0 => ZIO.fail(UserNotFoundError("Unable to delete this user as the username does not exist"))
      case _ => ZIO.unit
    }

  ).mapErrorCause { cause =>
    cause.squash match {
      case e: PSQLException if e.getSQLState == PSQLState.UNIQUE_VIOLATION.getState =>
        Cause.fail(UserAlreadyDeletedError(e.getMessage))
      case e: ServiceError =>
        Cause.fail(e)
      case e =>
        Cause.fail(DatabaseTransactionError(e.getMessage))
    }
  }

}

object UserService {
  val layer: ZLayer[UserRepositoryAlg, Nothing, UserServiceAlg] = ZLayer.derive[UserService]
}