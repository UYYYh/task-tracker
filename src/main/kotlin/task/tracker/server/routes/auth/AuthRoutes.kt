package task.tracker.server.routes.auth

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import task.tracker.server.auth.SessionRepository

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
