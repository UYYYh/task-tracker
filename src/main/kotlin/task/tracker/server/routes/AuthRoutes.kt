
package task.tracker.server.routes
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import task.tracker.server.auth.SessionRepository

@Serializable
data class LoginRequest(
    val username: String,
)

@Serializable
data class LoginResponse(
    val token: String,
)

fun Route.authRoutes(sessionRepository: SessionRepository) {
    // POST /login
    post("/login") {
        val body = call.receive<LoginRequest>()

        val session =
            sessionRepository.createSession(
                userIDRaw = body.username,
                ttlSeconds = 60L * 60L * 24L * 30L, // 30 days
            )

        call.respond(LoginResponse(token = session.token))
    }
}
