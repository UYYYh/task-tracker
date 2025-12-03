package task.tracker.server.routes.task

import ChangeDeadlineRequest
import ChangeDescriptionRequest
import CreateTaskRequest
import RenameRequest
import com.example.task.tracker.dto.TaskDTO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import task.tracker.app.TaskID
import task.tracker.app.TaskManager
import task.tracker.app.TaskSpec
import task.tracker.app.UserID
import task.tracker.app.UserTaskManager
import task.tracker.server.auth.AuthenticatedUser

// ===== Request DTOs =====

private fun ApplicationCall.requireUserID(): UserID {
    val principal =
        principal<AuthenticatedUser>()
            ?: throw IllegalStateException("No authenticated user; did you forget authenticate(\"auth-bearer\")?")
    return principal.toUserID()
}

private fun ApplicationCall.requireTaskID(): TaskID {
    val id =
        parameters["id"]
            ?: throw MissingRequestParameterException("id")
    return TaskID(id)
}

private fun ApplicationCall.requireUserTasks(taskManager: TaskManager): UserTaskManager {
    val userID = requireUserID()
    return taskManager.forUser(userID)
}

private suspend fun ApplicationCall.respondBool(success: Boolean) {
    if (success) {
        respond(HttpStatusCode.NoContent)
    } else {
        respond(HttpStatusCode.NotFound)
    }
}

fun Route.taskRoutes(taskManager: TaskManager) {
    get("/health") {
        call.respondText("OK")
    }

    authenticate("auth-bearer") {
        // GET /tasks -> list all tasks
        get("/tasks") {
            val userTasks = call.requireUserTasks(taskManager)
            val tasks: List<TaskDTO> = userTasks.listTasks()
            call.respond(tasks)
        }

        // POST /tasks -> create
        post("/tasks") {
            val userTasks = call.requireUserTasks(taskManager)
            val body = call.receive<CreateTaskRequest>()

            val spec =
                TaskSpec(
                    title = body.title,
                    description = body.description,
                    deadline = body.deadline,
                )

            val created: TaskDTO = userTasks.createTask(spec)
            call.respond(created)
        }

        // DELETE /tasks/{id}
        delete("/tasks/{id}") {
            val userTasks = call.requireUserTasks(taskManager)
            val taskID = call.requireTaskID()

            val success = userTasks.deleteTask(taskID)
            call.respondBool(success)
        }

        // PATCH /tasks/{id}/title
        patch("/tasks/{id}/title") {
            val userTasks = call.requireUserTasks(taskManager)
            val taskID = call.requireTaskID()
            val body = call.receive<RenameRequest>()

            val success = userTasks.renameTask(taskID, body.newTitle)
            call.respondBool(success)
        }

        // PATCH /tasks/{id}/description
        patch("/tasks/{id}/description") {
            val userTasks = call.requireUserTasks(taskManager)
            val taskID = call.requireTaskID()
            val body = call.receive<ChangeDescriptionRequest>()

            val success = userTasks.changeDescription(taskID, body.newDescription)
            call.respondBool(success)
        }

        // PATCH /tasks/{id}/deadline
        patch("/tasks/{id}/deadline") {
            val userTasks = call.requireUserTasks(taskManager)
            val taskID = call.requireTaskID()
            val body = call.receive<ChangeDeadlineRequest>()

            val success = userTasks.setDeadline(taskID, body.deadline)
            call.respondBool(success)
        }

        // PATCH /tasks/{id}/complete
        patch("/tasks/{id}/complete") {
            val userTasks = call.requireUserTasks(taskManager)
            val taskID = call.requireTaskID()

            val success = userTasks.completeTask(taskID, at = null)
            call.respondBool(success)
        }

        // PATCH /tasks/{id}/uncomplete
        patch("/tasks/{id}/uncomplete") {
            val userTasks = call.requireUserTasks(taskManager)
            val taskID = call.requireTaskID()

            val success = userTasks.uncompleteTask(taskID)
            call.respondBool(success)
        }
    }
}
