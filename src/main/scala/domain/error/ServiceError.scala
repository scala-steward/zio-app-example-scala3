package domain.error

import zio.schema.*
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.schema.{DeriveSchema, Schema}
import zio.*

sealed abstract class ServiceError(message: String) extends Exception(message)

final case class ToDomainError(message: String) extends ServiceError(message)

object ToDomainError {
  given schemaDepStatus: Schema[ToDomainError] = DeriveSchema.gen // used for httpContentCodec

  given toDomainErrorEncoder: JsonEncoder[ToDomainError] = DeriveJsonEncoder.gen[ToDomainError]

  given toDomainErrorDecoder: JsonDecoder[ToDomainError] = DeriveJsonDecoder.gen[ToDomainError]
}

final case class UsernameDuplicateError(message: String) extends ServiceError(message)

object UsernameDuplicateError {
  given schemaDepStatus: Schema[UsernameDuplicateError] = DeriveSchema.gen // used for httpContentCodec

  given usernameDuplicateErrorEncoder: JsonEncoder[UsernameDuplicateError] = DeriveJsonEncoder.gen[UsernameDuplicateError]

  given usernameDuplicateErrorDecoder: JsonDecoder[UsernameDuplicateError] = DeriveJsonDecoder.gen[UsernameDuplicateError]
}