package ross.rosstudent

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        
        get("/api/health") {
            call.respondText("API is healthy", ContentType.Text.Plain)
        }
        
        get("/api/status") {
            call.respondText("API is running successfully", ContentType.Text.Plain)
        }
        
        get("/api/version") {
            call.respondText("Version: 0.0.1", ContentType.Text.Plain)
        }
    }
}
