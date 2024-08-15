package database.service

import database.repository.StatusRepositoryAlg
import http.server.endpoint.HealthCheckEndpointsSpec.{suite, test}
import util.generators.Generators
import zio.*
import zio.jdbc.{ZConnection, ZConnectionPool}
import zio.test.*


object StatusServiceSpec extends ZIOSpecDefault with Generators {

  private def mockRepo(select1Resposnse: URIO[ZConnection, Option[Int]]) = ZLayer.succeed(
    new StatusRepositoryAlg:
      override def select1(): URIO[ZConnection, Option[Int]] = select1Resposnse
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("StatusService")(
      select1Tests
    )

  private val select1Tests = suite("select1")(
    test("returns true when the repository responds with Some(1)") {
      (for {
        isDBLive <- ZIO.serviceWithZIO[StatusServiceAlg](_.isDBLive)
      } yield assertTrue(isDBLive))
        .provide(
          mockRepo(ZIO.succeed(Option(1))),
          ZConnectionPool.h2test,
          StatusService.live
        )
    },
    test("returns false when the repository responds with None") {
      (for {
        isDBLive <- ZIO.serviceWithZIO[StatusServiceAlg](_.isDBLive)
      } yield assertTrue(!isDBLive))
        .provide(
          mockRepo(ZIO.none),
          ZConnectionPool.h2test,
          StatusService.live
        )
    }
  )
}
