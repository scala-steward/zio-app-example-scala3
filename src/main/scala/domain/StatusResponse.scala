package domain

import zio.*
import zio.json.*
import zio.schema.*

final case class StatusResponse(output: String)

object StatusResponse {
  given schema: Schema[StatusResponse] = DeriveSchema.gen // used for httpContentCodec

  /***
   * json encoding/decoding
   */
  given statusResponseEncoder: JsonEncoder[StatusResponse] = DeriveJsonEncoder.gen[StatusResponse] // used for json marshalling

  given statusResponseDecoder: JsonDecoder[StatusResponse] = DeriveJsonDecoder.gen[StatusResponse] // used for json marshalling
}