package domain

import zio.*
import zio.json.*
import zio.schema.*

type Dependency = String // todo: switch to value class or scala 3 equivalent
final case class DependenciesStatusResponse(output: Map[Dependency, String])

object DependenciesStatusResponse {
  given schemaDepStatus: Schema[DependenciesStatusResponse] = DeriveSchema.gen // used for httpContentCodec

  /***
   * json encoding/decoding
   */
  given depStatusResponseEncoder: JsonEncoder[DependenciesStatusResponse] = DeriveJsonEncoder.gen[DependenciesStatusResponse]

  given depStatusResponseDecoder: JsonDecoder[DependenciesStatusResponse] = DeriveJsonDecoder.gen[DependenciesStatusResponse]
}