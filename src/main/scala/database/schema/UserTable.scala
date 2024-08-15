package database.schema

import zio.*
import zio.jdbc.*
import zio.schema.Schema.Field
import zio.schema.{Schema, TypeId}

final case class UserTable(id: Int, userName: String, firstName: String, lastName: String)

object UserTable {

  given userTableSchema: Schema[UserTable] =
    Schema.CaseClass4[Int, String, String, String, UserTable](
      TypeId.parse("UserTable"),
      Field[UserTable, Int]("id", Schema.primitive[Int], get0 = _.id, set0 = (u, v) => u.copy(id = v)),
      Field[UserTable, String]("user_name", Schema.primitive[String], get0 = _.userName, set0 = (u, v) => u.copy(userName = v)),
      Field[UserTable, String]("first_name", Schema.primitive[String], get0 = _.firstName, set0 = (u, v) => u.copy(firstName = v)),
      Field[UserTable, String]("last_name", Schema.primitive[String], get0 = _.lastName, set0 = (u, v) => u.copy(lastName = v)),
      (id, uName, fName, lName) => UserTable(id, uName, fName, lName)
    )

  given userTableJdbcDecoder: JdbcDecoder[UserTable] = JdbcDecoder.fromSchema

}
