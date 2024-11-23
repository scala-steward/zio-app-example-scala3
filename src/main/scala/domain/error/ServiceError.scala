package domain.error

import zio.*
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.schema.*

sealed abstract class ServiceError(message: String) extends Exception(message)

final case class ToDomainError(message: String) extends ServiceError(message)

object ToDomainError {
  given toDomainErrorSchema: Schema[ToDomainError] = DeriveSchema.gen // used for httpContentCodec
}

final case class UserNotInsertedError(message: String) extends ServiceError(message)

object UserNotInsertedError {
  given databaseTransactionErrorSchema: Schema[UserNotInsertedError] = DeriveSchema.gen
}

final case class DatabaseTransactionError(message: String) extends ServiceError(message)

object DatabaseTransactionError {
  given databaseTransactionErrorSchema: Schema[DatabaseTransactionError] = DeriveSchema.gen
}

final case class UserAlreadyDeletedError(message: String) extends ServiceError(message)

object UserAlreadyDeletedError {
  given databaseTransactionErrorSchema: Schema[UserAlreadyDeletedError] = DeriveSchema.gen
}

final case class UserNotFoundError(message: String) extends ServiceError(message)

object UserNotFoundError {
  given databaseTransactionErrorSchema: Schema[UserNotFoundError] = DeriveSchema.gen
}

final case class UsernameDuplicateError(message: String) extends ServiceError(message)

object UsernameDuplicateError {
  given usernameDuplicateErrorSchema: Schema[UsernameDuplicateError] = DeriveSchema.gen
}