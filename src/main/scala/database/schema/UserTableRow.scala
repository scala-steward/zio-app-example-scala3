package database.schema

import domain.User
import io.scalaland.chimney.dsl.*
import zio.*
import zio.jdbc.*
import zio.schema.Schema.Field
import zio.schema.{Schema, TypeId}

final case class UserTableRow(id: Int, userName: String, firstName: String, lastName: String)

object UserTableRow {

  given userTableRowSchema: Schema[UserTableRow] =
    Schema.CaseClass4[Int, String, String, String, UserTableRow](
      TypeId.parse("database.schema.UserTableRow"),
      Field[UserTableRow, Int]("id", Schema.primitive[Int], get0 = _.id, set0 = (u, v) => u.copy(id = v)),
      Field[UserTableRow, String]("user_name", Schema.primitive[String], get0 = _.userName, set0 = (u, v) => u.copy(userName = v)),
      Field[UserTableRow, String]("first_name", Schema.primitive[String], get0 = _.firstName, set0 = (u, v) => u.copy(firstName = v)),
      Field[UserTableRow, String]("last_name", Schema.primitive[String], get0 = _.lastName, set0 = (u, v) => u.copy(lastName = v)),
      (id, userName, firstName, lastName) => UserTableRow(id, userName, firstName, lastName)
    )

  // Needed for reading from database, an example: .query[UserTableRow]
  given userTableRowJdbcDecoder: JdbcDecoder[UserTableRow] = JdbcDecoder.fromSchema

  def toDomain: UserTableRow => Task[User] = payload =>
    ZIO.attempt(payload.transformInto[User])
}
