package backend.repositories

import backend.db.DatabaseFactory.dbQuery
import backend.models.User
import backend.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

class UserRepository {
    suspend fun createUser(user: User): User? = dbQuery {
        try {
            val insertStatement = Users.insert {
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[middleName] = user.middleName
                it[email] = user.email
                it[college] = user.college
                it[group] = user.group
                it[password] = user.password
                it[role] = user.role
                it[createdAt] = user.createdAt
            }
            
            val insertedUserId = insertStatement.resultedValues?.singleOrNull()?.get(Users.userId)
            if (insertedUserId != null) {
                return@dbQuery User(
                    userId = insertedUserId,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    middleName = user.middleName,
                    email = user.email,
                    college = user.college,
                    group = user.group,
                    password = user.password,
                    role = user.role,
                    createdAt = user.createdAt
                )
            } else {
                println("Ошибка: не удалось получить ID созданного пользователя")
                return@dbQuery null
            }
        } catch (e: Exception) {
            println("Ошибка при создании пользователя: ${e.message}")
            e.printStackTrace()
            return@dbQuery null
        }
    }

    suspend fun findUserByName(firstName: String, lastName: String): User? = dbQuery {
        Users.select { (Users.firstName eq firstName) and (Users.lastName eq lastName) }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun findUserByEmail(email: String): User? = dbQuery {
        Users.select { Users.email eq email }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun findUserById(id: Int): User? = dbQuery {
        Users.select { Users.userId eq id }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun getAllUsers(): List<User> = dbQuery {
        Users.selectAll().map { rowToUser(it) }
    }

    suspend fun getStudentsByGroup(groupName: String): List<User> = dbQuery {
        Users.select { Users.group eq groupName }
            .map { rowToUser(it) }
    }

    suspend fun deleteUser(id: Int): Boolean = dbQuery {
        Users.deleteWhere { Users.userId eq id } > 0
    }

    private fun rowToUser(row: ResultRow): User {
        return User(
            userId = row[Users.userId],
            firstName = row[Users.firstName],
            lastName = row[Users.lastName],
            middleName = row[Users.middleName],
            email = row[Users.email],
            college = row[Users.college],
            group = row[Users.group],
            password = row[Users.password],
            role = row[Users.role],
            createdAt = row[Users.createdAt]
        )
    }
} 