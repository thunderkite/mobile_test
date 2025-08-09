package backend.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import backend.repositories.UserRepository
import backend.routes.MessageResponse
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class UserResponse(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val email: String,
    val college: String,
    val group: String,
    val role: String,
    val createdAt: String
)

@Serializable
data class StudentResponse(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val group: String
)

@Serializable
data class StudentsListResponse(
    val students: List<StudentResponse>
)

fun Route.userRoutes(userRepository: UserRepository) {
    // Получить список всех пользователей
    get("/users") {
        val users = userRepository.getAllUsers().map { user ->
            UserResponse(
                userId = user.userId,
                firstName = user.firstName,
                lastName = user.lastName,
                middleName = user.middleName,
                email = user.email,
                college = user.college,
                group = user.group,
                role = user.role,
                createdAt = user.createdAt.toString()
            )
        }
        call.respond(users)
    }

    // Получить конкретного пользователя по ID
    get("/users/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("Некорректный ID"))
            return@get
        }

        val user = userRepository.findUserById(id)
        if (user != null) {
            call.respond(UserResponse(
                userId = user.userId,
                firstName = user.firstName,
                lastName = user.lastName,
                middleName = user.middleName,
                email = user.email,
                college = user.college,
                group = user.group,
                role = user.role,
                createdAt = user.createdAt.toString()
            ))
        } else {
            call.respond(HttpStatusCode.NotFound, MessageResponse("Пользователь не найден"))
        }
    }

    // Получить список студентов по группе
    get("/students") {
        val group = call.request.queryParameters["group"]
        
        if (group.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("Не указана группа"))
            return@get
        }
        
        val students = userRepository.getStudentsByGroup(group).map { user ->
            StudentResponse(
                id = user.userId,
                firstName = user.firstName,
                lastName = user.lastName,
                middleName = user.middleName,
                group = user.group
            )
        }
        
        call.respond(StudentsListResponse(students))
    }

    // Удалить пользователя
    delete("/users/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, MessageResponse("Некорректный ID"))
            return@delete
        }

        val success = userRepository.deleteUser(id)
        if (success) {
            call.respond(HttpStatusCode.OK, MessageResponse("Пользователь удален"))
        } else {
            call.respond(HttpStatusCode.NotFound, MessageResponse("Пользователь не найден"))
        }
    }
} 