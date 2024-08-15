package database

import com.dimafeng.testcontainers.PostgreSQLContainer
import database.repository.{UserRepository, UserRepositoryAlg}
import database.schema.UserTable
import domain.{PortDetails, User}
import org.flywaydb.core.api.output.ValidateResult
import org.testcontainers.containers
import _root_.util.*
import zio.*
import zio.jdbc.*
import zio.test.*
import util.ZConnectionPoolWrapper

object UserRepositoryITSpec extends ZIOSpecDefault {

  private def connectionPoolConfigLayer(postgresContainer: containers.PostgreSQLContainer[?]) = ZConnectionPoolWrapper.connectionPool(
    postgresContainer.getHost,
    postgresContainer.getMappedPort(PortDetails.PostgresPort.port),
    postgresContainer.getDatabaseName,
    postgresContainer.getUsername,
    postgresContainer.getPassword
  )

  override def spec =
    suite("UserRepository")(
      test("can successfully insert into a user") {
        TestContainerResource.postgresResource.flatMap { postgresContainer =>
          (for {
            insertUser <- ZIO.serviceWith[UserRepositoryAlg](_.insertUser)
            flyway <- FlywayResource.flywayResource(postgresContainer.getJdbcUrl, postgresContainer.getUsername, postgresContainer.getPassword)
            validationResult <- ZIO.attempt(flyway.validateWithResult())
            user = User("LimbMissing", "David", "Pratt")
            selectSqlFrag = sql"select * from user_table".query[UserTable]
            underTest <- transaction(
              insertUser(user) *> selectSqlFrag.selectAll
            )
          } yield assertTrue(
            validationResult.validationSuccessful,
            underTest match {
              case Chunk(userTableRow) =>
                userTableRow.userName == user.userName &&
                  userTableRow.firstName == user.firstName &&
                  userTableRow.lastName == user.lastName
            }
          )).provide(
            connectionPoolConfigLayer(postgresContainer),
            ZLayer.succeed(ZConnectionPoolConfig.default),
            Scope.default,
            UserRepository.live
          )
        }
      }
    ) @@ TestAspect.timeout(zio.Duration.fromSeconds(35))
}
