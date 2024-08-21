package program

import database.service.UserServiceAlg
import domain.User
import domain.error.*
import zio.jdbc.ZConnectionPool
import zio.{Chunk, ZIO, ZLayer}

trait UserProgramAlg {
  def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Unit]
  def getAllUsers: ZIO[ZConnectionPool, ServiceError, Chunk[User]]
}

final case class UserProgram(
                              private val userServiceAlg: UserServiceAlg
                            ) extends UserProgramAlg {
  
  override def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Unit] =
    userServiceAlg.insertUser(user)

  override def getAllUsers: ZIO[ZConnectionPool, ServiceError, Chunk[User]] = userServiceAlg.getAllUsers
}

object UserProgram {
  val live: ZLayer[UserServiceAlg, Nothing, UserProgramAlg] = ZLayer.fromFunction(
    (userServiceAlg: UserServiceAlg) => UserProgram.apply(userServiceAlg)
  )
}
