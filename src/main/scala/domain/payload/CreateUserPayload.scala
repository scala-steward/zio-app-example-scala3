package domain.payload

import domain.User
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.schema.{DeriveSchema, Schema}
import io.scalaland.chimney.dsl.*
import zio.*

final case class CreateUserPayload(
                                    userName: String,
                                    firstName: String,
                                    lastName: String
                                  )

object CreateUserPayload {
  given schemaDepStatus: Schema[CreateUserPayload] = DeriveSchema.gen // used for httpContentCodec

  /***
   * json encoding/decoding
   */
  given createUserPayloadEncoder: JsonEncoder[CreateUserPayload] = DeriveJsonEncoder.gen[CreateUserPayload]

//  given createUserPayloadDecoder: JsonDecoder[CreateUserPayload] = DeriveJsonDecoder.gen[CreateUserPayload]

  def toDomain: CreateUserPayload => Task[User] = payload => 
    ZIO.attempt(payload.transformInto[User])

}