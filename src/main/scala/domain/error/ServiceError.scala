package domain.error

import zio.schema.*
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.schema.{DeriveSchema, Schema}
import zio.*

sealed abstract class ServiceError(message: String) extends Exception(message)

object ServiceError {
  given serviceErrorSchema: Schema[ServiceError] = DeriveSchema.gen
}

final case class ToDomainError(message: String) extends ServiceError(message)

object ToDomainError {
  given toDomainErrorSchema: Schema[ToDomainError] = DeriveSchema.gen

  given toDomainErrorEncoder: JsonEncoder[ToDomainError] = DeriveJsonEncoder.gen[ToDomainError]

  given toDomainErrorDecoder: JsonDecoder[ToDomainError] = DeriveJsonDecoder.gen[ToDomainError]
}

final case class DatabaseTransactionError(message: String) extends ServiceError(message)

object DatabaseTransactionError {
  given toDomainErrorSchema: Schema[DatabaseTransactionError] = DeriveSchema.gen

  given toDomainErrorEncoder: JsonEncoder[DatabaseTransactionError] = DeriveJsonEncoder.gen[DatabaseTransactionError]

  given toDomainErrorDecoder: JsonDecoder[DatabaseTransactionError] = DeriveJsonDecoder.gen[DatabaseTransactionError]
}


final case class UsernameDuplicateError(message: String) extends ServiceError(message)

object UsernameDuplicateError {
  given usernameDuplicateErrorSchema: Schema[UsernameDuplicateError] = DeriveSchema.gen

  given usernameDuplicateErrorEncoder: JsonEncoder[UsernameDuplicateError] = DeriveJsonEncoder.gen[UsernameDuplicateError]

  given usernameDuplicateErrorDecoder: JsonDecoder[UsernameDuplicateError] = DeriveJsonDecoder.gen[UsernameDuplicateError]
}