package domain.response

import zio.*
import zio.json.*
import zio.schema.*

final case class SuccessfulResponse[A](output: A)

object SuccessfulResponse {

  given schema[A: Schema]: Schema[SuccessfulResponse[A]] = DeriveSchema.gen[SuccessfulResponse[A]] // used for httpContentCodec

  given statusResponseEncoder[A: JsonEncoder]: JsonEncoder[SuccessfulResponse[A]] = DeriveJsonEncoder.gen[SuccessfulResponse[A]] // used for json marshalling

  given statusResponseDecoder[A: JsonDecoder]: JsonDecoder[SuccessfulResponse[A]] = DeriveJsonDecoder.gen[SuccessfulResponse[A]] // used for json marshalling
}