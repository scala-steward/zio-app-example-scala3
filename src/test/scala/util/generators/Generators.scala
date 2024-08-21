package util.generators

import database.schema.UserTable
import domain.User
import domain.payload.CreateUserPayload
import zio.Chunk
import zio.test.Gen

trait Generators {
  
  private val nonEmptyAlphaNumString: Gen[Any, String] = Gen.alphaNumericStringBounded(1, 30)
  
  val userGen: Gen[Any, User] = for {
    userName <- nonEmptyAlphaNumString
    firstName <- nonEmptyAlphaNumString
    lastName <- nonEmptyAlphaNumString
  } yield User(userName, firstName, lastName)
  
  val userTableGen: Gen[Any, UserTable] = for {
    id <- Gen.int
    userName <- nonEmptyAlphaNumString
    firstName <- nonEmptyAlphaNumString
    lastName <- nonEmptyAlphaNumString
  } yield UserTable(id, userName, firstName, lastName)

  val chunkUserTableGen: Gen[Any, Chunk[UserTable]] = for {
    chunk <- Gen.chunkOfBounded(1, 45)(userTableGen)
  } yield chunk
  
  val createUserPayload: Gen[Any, CreateUserPayload] = for {
    userName <- nonEmptyAlphaNumString
    firstName <- nonEmptyAlphaNumString
    lastName <- nonEmptyAlphaNumString
  } yield CreateUserPayload(userName, firstName, lastName)

}
