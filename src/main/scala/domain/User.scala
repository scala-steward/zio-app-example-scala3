package domain

import zio.*
import zio.json.*
import zio.schema.*


final case class User(
                       userName: String,
                       firstName: String,
                       lastName: String
                     )

object User {
  //  given schemaDepStatus: Schema[User] = DeriveSchema.gen // used for httpContentCodec

  /**
   * json encoding/decoding
   */
  given userEncoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]

  given userDecoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]

}