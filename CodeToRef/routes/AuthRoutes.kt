package backend.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import backend.models.User
import backend.repositories.UserRepository
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val password: String
)

@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val email: String,
    val college: String,
    val group: String,
    val password: String,
    val role: String = "student"
)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class RegisterResponse(val userId: Int, val message: String)

@Serializable
data class LoginResponse(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val email: String,
    val role: String,
    val message: String
)

fun Route.authRoutes(userRepository: UserRepository) {
    post("/register") {
        try {
            println("Получен запрос на регистрацию")
            val request = call.receive<RegisterRequest>()
            println("Данные запроса: firstName=${request.firstName}, lastName=${request.lastName}, email=${request.email}, college=${request.college}, group=${request.group}, role=${request.role}")
            
            // Проверка, существует ли пользователь с таким именем и фамилией
            val existingUserByName = userRepository.findUserByName(request.firstName, request.lastName)
            if (existingUserByName != null) {
                println("Пользователь с именем ${request.firstName} ${request.lastName} уже существует")
                call.respond(HttpStatusCode.Conflict, MessageResponse("Пользователь с таким именем и фамилией уже существует"))
                return@post
            }
            
            // Проверка, существует ли пользователь с такой почтой
            val existingUserByEmail = userRepository.findUserByEmail(request.email)
            if (existingUserByEmail != null) {
                println("Пользователь с почтой ${request.email} уже существует")
                call.respond(HttpStatusCode.Conflict, MessageResponse("Пользователь с такой почтой уже существует"))
                return@post
            }

            // Создание нового пользователя
            val user = User(
                userId = 0, // ID присвоится автоматически
                firstName = request.firstName,
                lastName = request.lastName,
                middleName = request.middleName,
                email = request.email,
                college = request.college,
                group = request.group,
                password = request.password, // В реальном приложении нужно хешировать пароль
                role = request.role
            )
            println("Создаем пользователя: $user")

            val createdUser = userRepository.createUser(user)
            if (createdUser != null) {
                println("Пользователь успешно создан: userId=${createdUser.userId}")
                call.respond(HttpStatusCode.Created, RegisterResponse(createdUser.userId, "Пользователь успешно зарегистрирован"))
            } else {
                println("Ошибка при создании пользователя")
                call.respond(HttpStatusCode.InternalServerError, MessageResponse("Ошибка при регистрации пользователя"))
            }
        } catch (e: Exception) {
            println("Исключение при регистрации: ${e.message}")
            e.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, MessageResponse("Ошибка при регистрации пользователя: ${e.message}"))
        }
    }

    post("/login") {
        val request = call.receive<LoginRequest>()
        
        // Поиск пользователя по email
        if (request.email != null) {
            val user = userRepository.findUserByEmail(request.email)
            if (user != null && user.password == request.password) { // В реальном приложении сравнивать хеши
                call.respond(HttpStatusCode.OK, LoginResponse(
                    userId = user.userId,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    middleName = user.middleName,
                    email = user.email,
                    role = user.role,
                    message = "Успешный вход"
                ))
                return@post
            }
        }
        // Поиск пользователя по имени и фамилии
        else if (request.firstName != null && request.lastName != null) {
            val user = userRepository.findUserByName(request.firstName, request.lastName)
            if (user != null && user.password == request.password) { // В реальном приложении сравнивать хеши
                call.respond(HttpStatusCode.OK, LoginResponse(
                    userId = user.userId,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    middleName = user.middleName,
                    email = user.email,
                    role = user.role,
                    message = "Успешный вход"
                ))
                return@post
            }
        }
        
        call.respond(HttpStatusCode.Unauthorized, MessageResponse("Неверные учетные данные"))
    }
} 