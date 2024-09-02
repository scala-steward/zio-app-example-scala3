package domain.payload

import domain.User
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.Empty
import io.scalaland.chimney.dsl.*
import zio.*
import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.schema.Schema.primitive
import zio.schema.{DeriveSchema, Schema}

final case class CreateUserPayload(
                                    userName: String :| Not[Empty], // an example as to how Iron can be implemented
                                    firstName: String,
                                    lastName: String,
                                    address: Option[String]
                                  )

object CreateUserPayload {

  given nonEmptyStringSchema: Schema[String :| Not[Empty]] = primitive[String].transformOrFail(
    string => string.refineEither[Not[Empty]].left.map(_ => "String should not be empty"), // override the default error message
    refinedString =>
      val extractString: String = refinedString
      Right(extractString).withLeft[String]
  )

  given createUserPayloadSchema: Schema[CreateUserPayload] = DeriveSchema.gen // used for httpContentCodec

  given createUserPayloadEncoder: JsonEncoder[CreateUserPayload] = DeriveJsonEncoder.gen[CreateUserPayload]
  
  def toDomain: CreateUserPayload => Task[User] = payload =>
    ZIO.attempt(payload.transformInto[User])

}