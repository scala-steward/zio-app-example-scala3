package configuration

import configuration.ApplicationConfig.*

import zio.{Config, ConfigProvider, IO, ZLayer}
import zio.config.*
import zio.config.magnolia.*


trait ApplicationConfigAlg {
  def hoconConfig: IO[Config.Error, HoconConfig]
}

final case class ApplicationConfig(
                                    private val source: ConfigProvider
                                  ) extends ApplicationConfigAlg {


  private val exampleConfigAutomaticDerivation: zio.Config[HoconConfig] = deriveConfig[HoconConfig].mapKey(toKebabCase)

  override def hoconConfig: IO[Config.Error, HoconConfig] = source.load(exampleConfigAutomaticDerivation)

}

object ApplicationConfig {
  final case class DatabaseConfig(
                                   exposedPort: Int,
                                   host: String,
                                   jdbcUrl: String,
                                   user: String,
                                   password: String,
                                   database: String
                                 )
  
  final case class HoconConfig(
                                db: DatabaseConfig
                              )

  val live: ZLayer[ConfigProvider, Nothing, ApplicationConfigAlg] =
    zio.ZLayer.fromFunction((source: ConfigProvider) => ApplicationConfig(source))
}
