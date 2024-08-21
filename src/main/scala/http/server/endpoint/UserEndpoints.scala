package http.server.endpoint

import domain.*
import domain.error.*
import domain.payload.*
import domain.response.*
import program.*
import zio.*
import zio.http.*
import zio.http.Status.UnprocessableEntity
import zio.http.codec.*
import zio.http.endpoint.Endpoint
import zio.http.endpoint.EndpointMiddleware.None
import zio.jdbc.ZConnectionPool

trait UserEndpointsAlg {
  def endpoints: List[Endpoint[Unit, ? >: CreateUserPayload & Unit, ? >: ZNothing <: ServiceError, ? >: SuccessfulResponse & AllUsersResponse <: Product, None]]
  def routes: Routes[ZConnectionPool, Nothing]
}

final case class UserEndpoints(
                              private val userProgramAlg: UserProgramAlg
                              ) extends UserEndpointsAlg {
  
  /** *
   * #1 - inserts the user provided in the request payload
   */

// https://github.com/zio/zio-http/blob/283934e5282fc7dbb8f11f955d5bd733030005e2/zio-http-example/src/main/scala/example/endpoint/style/DeclarativeProgrammingExample.scala#L37
// https://github.com/zio/zio-http/blob/283934e5282fc7dbb8f11f955d5bd733030005e2/zio-http-example/src/main/scala/example/endpoint/EndpointWithMultipleErrorsUsingEither.scala#L46
// https://github.com/zio/zio-http/blob/283934e5282fc7dbb8f11f955d5bd733030005e2/zio-http-example/src/main/scala/example/endpoint/EndpointWithMultipleUnifiedErrors.scala
  private val insertUserEndpoints =
    Endpoint(Method.POST / Root / "user")
      .in[CreateUserPayload]
      .out[SuccessfulResponse](Status.Created)
      .outErrors[ServiceError](
        HttpCodec.error[ToDomainError](Status.UnprocessableEntity),
        HttpCodec.error[UsernameDuplicateError](Status.Conflict)
      )
  
  private val insertUserRoute = insertUserEndpoints.implement { (createUserPayload: CreateUserPayload) =>
    for {
      user <- CreateUserPayload.toDomain(createUserPayload).mapError(t => ToDomainError(t.getMessage))
      _ <- userProgramAlg.insertUser(user)
    }  yield SuccessfulResponse("success")
  }

  /** *
   * #2 - gets all the users that have been stored in the database
   */

  private val getAllUsersEndpoints =
    Endpoint(Method.GET / Root / "users")
      .out[AllUsersResponse](Status.Ok)

  private val getAllUsersRoute = getAllUsersEndpoints.implement { _ =>
    for {
      users <- userProgramAlg.getAllUsers.orDie
    } yield AllUsersResponse(users)
  }


  /** *
   * Returns the public endpoints and routes
   */
  override def endpoints: List[Endpoint[Unit, ? >: CreateUserPayload & Unit, ? >: ZNothing <: ServiceError, ? >: SuccessfulResponse & AllUsersResponse <: Product, None]] = List(
    insertUserEndpoints,
    getAllUsersEndpoints
  )

  override def routes: Routes[ZConnectionPool, Nothing] = Routes.fromIterable(List(
    insertUserRoute,
    getAllUsersRoute
  ))
}

object UserEndpoints {
  val live: ZLayer[UserProgramAlg, Nothing, UserEndpointsAlg] = ZLayer.fromFunction(
    (userProgramAlg: UserProgramAlg) => UserEndpoints.apply(userProgramAlg)
  )
}
