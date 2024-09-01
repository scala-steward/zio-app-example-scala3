package domain

import zio.*
import zio.json.*


final case class User(
                       userName: String,
                       firstName: String,
                       lastName: String,
                       address: Option[String]
                     )

object User {
  
  given userEncoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]

  given userDecoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]

}