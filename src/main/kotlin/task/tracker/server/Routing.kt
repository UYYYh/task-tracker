
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import task.tracker.app.TaskManager
import task.tracker.server.auth.SessionRepository
import task.tracker.server.routes.auth.authRoutes
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
