package backend.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import backend.models.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init() {
        try {
            val driverClassName = "org.postgresql.Driver"
            val jdbcURL = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/rosstudentdb"
            val user = System.getenv("DATABASE_USER") ?: "postgres"
            val password = System.getenv("DATABASE_PASSWORD") ?: "postgres"
            
            println("Подключение к БД: $jdbcURL, пользователь: $user")
            
            val database = Database.connect(
                url = jdbcURL,
                driver = driverClassName,
                user = user,
                password = password
            )
            
            println("Соединение с БД установлено, создаем схему")

            transaction(database) {
                println("Создание таблиц: начало")
                SchemaUtils.drop(Users)
                SchemaUtils.create(Users)
                println("Создание таблиц: успешно")
            }
            
            println("Инициализация БД завершена успешно")
        } catch (e: Exception) {
            println("Ошибка при инициализации БД: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { 
            try {
                block()
            } catch (e: Exception) {
                println("Ошибка при выполнении запроса к БД: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
} 