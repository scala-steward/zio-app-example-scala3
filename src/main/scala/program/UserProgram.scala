package program

import database.repository.StatusRepositoryAlg
import database.service.{StatusServiceAlg, UserServiceAlg}
import domain.User
import domain.error.*
import zio.jdbc.{ZConnection, ZConnectionPool}
import zio.{ZIO, ZLayer}

trait UserProgramAlg {
  def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Unit]
}

final case class UserProgram(
                              private val userServiceAlg: UserServiceAlg
                            ) extends UserProgramAlg {
  
  override def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Unit] =
    for {
      _ <- userServiceAlg.insertUser(user)
    } yield ()
}

object UserProgram {
  val live: ZLayer[UserServiceAlg, Nothing, UserProgramAlg] = ZLayer.fromFunction(
    (userServiceAlg: UserServiceAlg) => UserProgram.apply(userServiceAlg)
  )
}
