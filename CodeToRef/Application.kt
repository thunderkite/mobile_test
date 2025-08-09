package backend

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.swagger.*
import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*
import backend.db.DatabaseFactory
import backend.repositories.UserRepository
import backend.routes.authRoutes
import backend.routes.userRoutes
import backend.admin.adminRoutes
import kotlinx.serialization.json.Json
import java.net.InetAddress
import java.net.NetworkInterface

fun main() {
    // Получаем информацию о сетевых интерфейсах для диагностики
    val localHost = InetAddress.getLocalHost()
    println("Локальный хост: ${localHost.hostName}, IP: ${localHost.hostAddress}")
    
    // Находим первый IPv4-адрес, начинающийся на 192.168
    val wifiIp = NetworkInterface.getNetworkInterfaces().toList()
        .flatMap { it.inetAddresses.toList() }
        .find { !it.isLoopbackAddress && it.hostAddress.startsWith("192.168.") }
        ?.hostAddress
    
    println("\n=== Информация для подключения ===")
    println("Для подключения с телефона используйте:")
    println("HTTP: http://$wifiIp:8081")
    println("Swagger UI: http://$wifiIp:8081/swagger")
    println("Админ-панель: http://$wifiIp:8081/admin")
    println("===============================\n")
    
    val networkInterfaces = NetworkInterface.getNetworkInterfaces()
    println("Доступные сетевые интерфейсы:")
    networkInterfaces.toList().forEach { networkInterface ->
        println("  Интерфейс: ${networkInterface.name}, ${networkInterface.displayName}")
        networkInterface.inetAddresses.toList().forEach { address ->
            println("    Адрес: ${address.hostAddress}")
        }
    }
    
    // Запускаем HTTP сервер
    println("\nЗапуск HTTP сервера на порту 8081...")
    val server = embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
        configureServer()
    }
    
    // Запускаем сервер
    server.start(wait = true)
}

private fun Application.configureServer() {
    println("Настройка сервера...")
        
        // Инициализируем базу данных
        println("Подключение к базе данных...")
        DatabaseFactory.init()
        println("Подключение к базе данных выполнено")
        
        // Репозиторий пользователей
        val userRepository = UserRepository()

    // Настраиваем CORS
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.Accept)
        allowCredentials = true
        allowNonSimpleContentTypes = true
        maxAgeInSeconds = 3600
    }
        
        // Настраиваем плагины
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
        
        // Настраиваем маршруты
        routing {
            // Swagger UI
            swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    
        // Тестовый маршрут для проверки доступности
        get("/api/test") {
            val clientHost = call.request.local.remoteHost
            println("Получен запрос от: $clientHost")
            call.respond(mapOf("status" to "success", "message" to "API доступно!", "client" to clientHost))
        }
            
            // Редирект с корня на админку
            get("/") {
                call.respondRedirect("/admin", permanent = false)
            }
            
            // Маршруты аутентификации
            authRoutes(userRepository)
            
            // Маршруты управления пользователями
            userRoutes(userRepository)
            
            // Маршруты админ-панели
            adminRoutes(userRepository)
        }
        
        println("Сервер настроен и готов принимать запросы")
} 