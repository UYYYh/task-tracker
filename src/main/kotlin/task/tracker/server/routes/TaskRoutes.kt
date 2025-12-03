package task.tracker.server.routes

import com.example.task.tracker.dto.TaskDTO
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import task.tracker.app.TaskID
import task.tracker.app.TaskManager
import task.tracker.app.TaskSpec
import task.tracker.server.auth.AuthenticatedUser

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String = "",
    val deadline: Instant? = null,
)

fun Route.taskRoutes(taskManager: TaskManager) {
    // Everything inside here requires a valid Bearer token
    authenticate("auth-bearer") {
        // GET /tasks  -> list all tasks for this user
        get("/tasks") {
            val principal = call.principal<AuthenticatedUser>()!!
            val userId = principal.toUserId()

            val userTasks = taskManager.forUser(userId)
            val tasks: List<TaskDTO> = userTasks.listTasks()

            call.respond(tasks)
        }

        // POST /tasks  -> create a new task
        post("/tasks") {
            val principal = call.principal<AuthenticatedUser>()!!
            val userId = principal.toUserId()

            val body = call.receive<CreateTaskRequest>()
            val spec =
                TaskSpec(
                    title = body.title,
                    description = body.description,
                    deadline = body.deadline,
                )

            val userTasks = taskManager.forUser(userId)
            val created: TaskDTO = userTasks.createTask(spec)

            call.respond(created)
        }

        // DELETE /tasks/{id} -> delete a task
        delete("/tasks/{id}") {
            val principal = call.principal<AuthenticatedUser>()!!
            val userId = principal.toUserId()

            val idParam =
                call.parameters["id"]
                    ?: return@delete call.respondText(
                        "Missing id",
                        status = io.ktor.http.HttpStatusCode.BadRequest,
                    )

            val userTasks = taskManager.forUser(userId)
            val success = userTasks.deleteTask(TaskID(idParam))

            if (success) {
                call.respond(io.ktor.http.HttpStatusCode.NoContent)
            } else {
                call.respond(io.ktor.http.HttpStatusCode.NotFound)
            }
        }
    }
}
