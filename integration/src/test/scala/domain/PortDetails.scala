package domain

enum PortDetails(portValue: Int) {
  val port: Int = portValue
  case PostgresPort extends PortDetails(5432)
}
