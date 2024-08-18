package database.service

import database.repository.StatusRepositoryITSpec.{suite, test}
import database.repository.UserRepositoryITSpec.connectionPoolConfigLayer
import database.repository.{StatusRepository, StatusRepositoryAlg, UserRepository}
import database.util.ZConnectionPoolWrapper
import domain.error.UsernameDuplicateError
import domain.{PortDetails, User}
import org.testcontainers.containers
import util.{FlywayResource, TestContainerResource}
import zio.jdbc.{ZConnectionPoolConfig, transaction}
import zio.{Scope, ZIO, ZLayer}
import zio.test.{TestAspect, ZIOSpecDefault, assertTrue}

object UserServiceITSpec extends ZIOSpecDefault {


  private def connectionPoolConfigLayer(postgresContainer: containers.PostgreSQLContainer[?]) = ZConnectionPoolWrapper.connectionPool(
    postgresContainer.getHost,
    postgresContainer.getMappedPort(PortDetails.PostgresPort.port),
    postgresContainer.getDatabaseName,
    postgresContainer.getUsername,
    postgresContainer.getPassword
  )

  override def spec =
    suite("StatusRepository")(
      test("can successfully insert the user") {
        TestContainerResource.postgresResource.flatMap { postgresContainer =>
          (for {
            flyway <- FlywayResource.flywayResource(postgresContainer.getJdbcUrl, postgresContainer.getUsername, postgresContainer.getPassword)
            validationResult <- ZIO.attempt(flyway.validateWithResult())
            user <- ZIO.succeed(User("limbmissing", "David", "Pratt"))
            _ <- ZIO.serviceWithZIO[UserServiceAlg](_.insertUser(user))
          } yield assertTrue(
            validationResult.validationSuccessful
          )).provide(
            connectionPoolConfigLayer(postgresContainer),
            ZLayer.succeed(ZConnectionPoolConfig.default),
            Scope.default,
            UserRepository.live,
            UserService.live
          )
        }
      },
      test("cannot insert the same username more than once") {
        TestContainerResource.postgresResource.flatMap { postgresContainer =>
          (for {
            flyway <- FlywayResource.flywayResource(postgresContainer.getJdbcUrl, postgresContainer.getUsername, postgresContainer.getPassword)
            validationResult <- ZIO.attempt(flyway.validateWithResult())
            user = User("limbmissing", "David", "Pratt")
            underTest <- ZIO.service[UserServiceAlg]
            _ <- underTest.insertUser(user)
            error <- underTest.insertUser(user).flip
          } yield assertTrue(
            validationResult.validationSuccessful,
            error.isInstanceOf[UsernameDuplicateError]
          )).provide(
            connectionPoolConfigLayer(postgresContainer),
            ZLayer.succeed(ZConnectionPoolConfig.default),
            Scope.default,
            UserRepository.live,
            UserService.live
          )
        }
      }
    ) @@ TestAspect.timeout(zio.Duration.fromSeconds(35))

}
