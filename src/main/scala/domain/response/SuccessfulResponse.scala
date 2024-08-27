package domain.response

import zio.*
import zio.json.*
import zio.schema.*

final case class SuccessfulResponse(output: String)

object SuccessfulResponse {
  given schema: Schema[SuccessfulResponse] = DeriveSchema.gen // used for httpContentCodec
  
  given statusResponseEncoder: JsonEncoder[SuccessfulResponse] = DeriveJsonEncoder.gen[SuccessfulResponse] // used for json marshalling

  given statusResponseDecoder: JsonDecoder[SuccessfulResponse] = DeriveJsonDecoder.gen[SuccessfulResponse] // used for json marshalling
}