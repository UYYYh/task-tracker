package task.tracker.server.routes.task

import CreateTaskRequest
import UpdateTaskRequest
import com.example.task.tracker.dto.TaskDTO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
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

        // PATCH /tasks/{id}/ -> updates task
        patch("/tasks/{id}") {
            val userTasks = call.requireUserTasks(taskManager)
            val taskID = call.requireTaskID()
            val body = call.receive<UpdateTaskRequest>()

            val success =
                userTasks.updateTask(
                    taskID,
                    body.title,
                    body.description,
                    body.deadline,
                    body.completionTime,
                )
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
