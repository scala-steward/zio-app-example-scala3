package domain.error

import zio.*
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.schema.*

sealed abstract class ServiceError(message: String) extends Exception(message)

object ServiceError {
  given serviceErrorSchema: Schema[ServiceError] = DeriveSchema.gen
}

final case class ToDomainError(message: String) extends ServiceError(message)

object ToDomainError {
  given toDomainErrorSchema: Schema[ToDomainError] = DeriveSchema.gen // used for httpContentCodec
}

final case class DatabaseTransactionError(message: String) extends ServiceError(message)

object DatabaseTransactionError {
  given toDomainErrorSchema: Schema[DatabaseTransactionError] = DeriveSchema.gen
}


final case class UsernameDuplicateError(message: String) extends ServiceError(message)

object UsernameDuplicateError {
  given usernameDuplicateErrorSchema: Schema[UsernameDuplicateError] = DeriveSchema.gen
}