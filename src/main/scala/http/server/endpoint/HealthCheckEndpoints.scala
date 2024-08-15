package http.server.endpoint

import domain.{DependenciesStatusResponse, StatusResponse}
import program.HealthProgramAlg
import zio.*
import zio.http.*
import zio.http.codec.HttpContentCodec
import zio.http.endpoint.Endpoint
import zio.http.endpoint.EndpointMiddleware.None
import zio.jdbc.ZConnectionPool


trait HealthCheckEndpointsAlg {
  def endpoints: List[Endpoint[Unit, Unit, ZNothing, ? >: StatusResponse & DependenciesStatusResponse <: Product, None]]
  def routes: Routes[ZConnectionPool, Response]
}

final case class HealthCheckEndpoints(
                                     private val healthProgram: HealthProgramAlg
                                     ) extends HealthCheckEndpointsAlg {


  /** *
   * #1 - gets the status of dependencies. i.e. Database etc
   */

  private val getDependenciesStatusEndpoint =
    Endpoint(Method.GET / Root / "status" / "dependencies").out[DependenciesStatusResponse]

  private val getDependenciesStatusRoute = getDependenciesStatusEndpoint.implement { _ =>
    for {
      depStatusMap <- healthProgram.getStatuses.orDie
    } yield DependenciesStatusResponse(depStatusMap)
  }

  /***
   * Second set of endpoints and routes
   */

  private val getStatusEndpoint =
    Endpoint(Method.GET / Root / "status").out[StatusResponse]

  private val getStatusRoute = getStatusEndpoint.implement { _ =>
    ZIO.succeed(StatusResponse("Ok"))
  }
  
  /***
   * Returns the public endpoints and routes
   */
  def endpoints: List[Endpoint[Unit, Unit, ZNothing, ? >: StatusResponse & DependenciesStatusResponse <: Product, None]] = List(
    getStatusEndpoint,
    getDependenciesStatusEndpoint
  )

  def routes: Routes[ZConnectionPool, Response] = Routes.fromIterable(List(
    getStatusRoute,
    getDependenciesStatusRoute
  ))

}

object HealthCheckEndpoints {
  val live: ZLayer[HealthProgramAlg, Nothing, HealthCheckEndpointsAlg] = ZLayer.fromFunction(
    (healthProgramAlg: HealthProgramAlg) => HealthCheckEndpoints.apply(healthProgramAlg)
  )
}
