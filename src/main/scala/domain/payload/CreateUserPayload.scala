package domain.payload

import domain.User
import io.scalaland.chimney.dsl.*
import zio.*
import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.schema.{DeriveSchema, Schema}

final case class CreateUserPayload(
                                    userName: String,
                                    firstName: String,
                                    lastName: String,
                                    address: Option[String]
                                  )

object CreateUserPayload {
  given schemaDepStatus: Schema[CreateUserPayload] = DeriveSchema.gen // used for httpContentCodec

  given createUserPayloadEncoder: JsonEncoder[CreateUserPayload] = DeriveJsonEncoder.gen[CreateUserPayload]
  
  def toDomain: CreateUserPayload => Task[User] = payload =>
    ZIO.attempt(payload.transformInto[User])

}