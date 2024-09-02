package util.generators

import database.schema.UserTableRow
import domain.User
import domain.payload.CreateUserPayload
import zio.Chunk
import zio.test.Gen
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.Empty

trait Generators {

  private val nonEmptyAlphaNumString: Gen[Any, String] = Gen.alphaNumericStringBounded(1, 30)

  val userGen: Gen[Any, User] = for {
    userName <- nonEmptyAlphaNumString
    firstName <- nonEmptyAlphaNumString
    lastName <- nonEmptyAlphaNumString
    address <- Gen.option(nonEmptyAlphaNumString)
  } yield User(userName, firstName, lastName, address)

  val userTableGen: Gen[Any, UserTableRow] = for {
    id <- Gen.int
    userName <- nonEmptyAlphaNumString
    firstName <- nonEmptyAlphaNumString
    lastName <- nonEmptyAlphaNumString
    address <- Gen.option(nonEmptyAlphaNumString)
  } yield UserTableRow(id, userName, firstName, lastName, address)

  val chunkUserTableGen: Gen[Any, Chunk[UserTableRow]] = for {
    chunk <- Gen.chunkOfBounded(1, 45)(userTableGen)
  } yield chunk


  val nonEmptyCreateUserPayload: Gen[Any, CreateUserPayload] = for {
    userName <- nonEmptyAlphaNumString.map(_.refineUnsafe[Not[Empty]])
    firstName <- nonEmptyAlphaNumString
    lastName <- nonEmptyAlphaNumString
    address <- Gen.option(nonEmptyAlphaNumString)
  } yield CreateUserPayload(userName, firstName, lastName, address)

}
