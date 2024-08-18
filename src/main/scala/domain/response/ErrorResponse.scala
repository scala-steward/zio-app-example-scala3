package domain.response

import zio.*
import zio.json.*
import zio.schema.*

final case class ErrorResponse(output: String)

object ErrorResponse {
  given schema: Schema[ErrorResponse] = DeriveSchema.gen // used for httpContentCodec

  /***
   * json encoding/decoding
   */
  given statusResponseEncoder: JsonEncoder[ErrorResponse] = DeriveJsonEncoder.gen[ErrorResponse] // used for json marshalling

//  given statusResponseDecoder: JsonDecoder[ErrorResponse] = DeriveJsonDecoder.gen[ErrorResponse] // used for json marshalling
}