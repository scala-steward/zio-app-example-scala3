package domain.response

import domain.User
import zio.*
import zio.json.*
import zio.schema.*

final case class AllUsersResponse(users: Chunk[User])

object AllUsersResponse {
  given schemaDepStatus: Schema[AllUsersResponse] = DeriveSchema.gen // used for httpContentCodec
  
  given allUsersResponseEncoder: JsonEncoder[AllUsersResponse] = DeriveJsonEncoder.gen[AllUsersResponse]

  given allUsersResponseDecoder: JsonDecoder[AllUsersResponse] = DeriveJsonDecoder.gen[AllUsersResponse]
}