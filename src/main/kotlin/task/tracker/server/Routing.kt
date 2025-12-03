// e.g. in com.example.plugins.Routing.kt
import io.ktor.server.application.*
import io.ktor.server.routing.*
import task.tracker.app.TaskManager
import task.tracker.server.auth.SessionRepository
import task.tracker.server.routes.authRoutes
import task.tracker.server.routes.task.taskRoutes

fun Application.configureRouting(
    taskManager: TaskManager,
    sessionRepository: SessionRepository,
) {
    routing {
        authRoutes(sessionRepository) // POST /login etc.
        taskRoutes(taskManager) // /tasks, /tasks/{id}, all inside authenticate("auth-bearer")
    }
}
