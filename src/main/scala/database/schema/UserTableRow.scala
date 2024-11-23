package database.schema

import domain.User
import io.scalaland.chimney.dsl.*
import izumi.reflect.Tag as IzumiTag
import zio.*
import zio.jdbc.*
import zio.schema.Schema.{Field, primitive}
import zio.schema.{Schema, StandardType, TypeId}

final case class UserTableRow(id: Int, userName: String, firstName: String, lastName: String, maybeAddress: Option[String])

object UserTableRow {
  private def nullableToOptionSchema[A: IzumiTag](implicit standardType: StandardType[A]): Schema[Option[A]] = primitive[A].transformOrFail(
    {
      case null => Right(None)
      case default => Right(Some(default))
    },
    maybeA => {
      val typeName = IzumiTag[A].tag.shortName
      maybeA.toRight[String](s"Failed to retrieve value from Optional $typeName")
    }
  )

  given userTableRowSchema: Schema[UserTableRow] =
    Schema.CaseClass5[Int, String, String, String, Option[String], UserTableRow](
      TypeId.parse("database.schema.UserTableRow"),
      Field[UserTableRow, Int]("id", Schema.primitive[Int], get0 = _.id, set0 = (u, v) => u.copy(id = v)),
      Field[UserTableRow, String]("user_name", Schema.primitive[String], get0 = _.userName, set0 = (u, v) => u.copy(userName = v)),
      Field[UserTableRow, String]("first_name", Schema.primitive[String], get0 = _.firstName, set0 = (u, v) => u.copy(firstName = v)),
      Field[UserTableRow, String]("last_name", Schema.primitive[String], get0 = _.lastName, set0 = (u, v) => u.copy(lastName = v)),
      Field[UserTableRow, Option[String]]("address", nullableToOptionSchema[String], get0 = _.maybeAddress, set0 = (u, v) => u.copy(maybeAddress = v)),
      (id, userName, firstName, lastName, maybeAddress) => UserTableRow(id, userName, firstName, lastName, maybeAddress)
    )

  // Needed for reading from database, an example: .query[UserTableRow]
  given userTableRowJdbcDecoder: JdbcDecoder[UserTableRow] = JdbcDecoder.fromSchema

  def toDomain: UserTableRow => Task[User] = payload =>
    ZIO.attempt(
      payload
        .into[User]
        .withFieldRenamed(_.maybeAddress, _.address)
        .transform
    )
}
