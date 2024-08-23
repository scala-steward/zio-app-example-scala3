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
                              private val userService: UserServiceAlg
                            ) extends UserProgramAlg {
  
  override def insertUser(user: User): ZIO[ZConnectionPool, ServiceError, Unit] =
    userService.insertUser(user)

  override def getAllUsers: ZIO[ZConnectionPool, ServiceError, Chunk[User]] = userService.getAllUsers
}

object UserProgram {
  val live: ZLayer[UserServiceAlg, Nothing, UserProgramAlg] = ZLayer.fromFunction(
    (userService: UserServiceAlg) => UserProgram.apply(userService)
  )
}
