// task-tracker-server/src/main/kotlin/task/tracker/server/auth/TokenAuth.kt

package task.tracker.server.auth

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer

fun Application.configureTokenAuth(sessionRepository: SessionRepository) {
    install(Authentication) {
        bearer("auth-bearer") {
            authenticate { tokenCredential ->
                val token = tokenCredential.token
                val session = sessionRepository.findByToken(token)
                if (session != null) {
                    AuthenticatedUser(session.userIDRaw)
                } else {
                    null // invalid/expired token
                }
            }
        }
    }
}
