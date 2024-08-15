package util.generators

import domain.User
import zio.test.Gen

trait Generators {
  
  private val nonEmptyAlphaNumString: Gen[Any, String] = Gen.alphaNumericStringBounded(1, 30)
  
  val userGen: Gen[Any, User] = for {
    userName <- nonEmptyAlphaNumString
    firstName <- nonEmptyAlphaNumString
    lastName <- nonEmptyAlphaNumString
  } yield User(userName, firstName, lastName)

}
