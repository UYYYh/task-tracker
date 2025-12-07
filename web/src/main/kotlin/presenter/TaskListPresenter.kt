package presenter

import api.TaskApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import model.TaskDTO
import util.toInstantOrNull
import view.TaskListView

class TaskListPresenter(
    private val api: TaskApi,
    private val view: TaskListView,
    private val scope: CoroutineScope = MainScope(),
) {
    private var currentTasks: List<TaskDTO> = emptyList()

    init {
        view.onLoginClicked = { username ->
            handleLogin(username)
        }

        view.onCreateTaskClicked = { title, description, deadlineRaw ->
            handleCreateTask(title, description, deadlineRaw)
        }

        view.onDeleteTaskClicked = { taskId ->
            handleDeleteTask(taskId)
        }

        view.onTaskClicked = onTaskClicked@{ taskId ->
            val task = currentTasks.find { it.id == taskId } ?: return@onTaskClicked
            view.showTaskDetails(task)
        }

        view.onTaskEditConfirmed = { taskId, title, description, deadlineRaw ->
            handleEditTask(taskId, title, description, deadlineRaw)
        }
    }

    fun start() {
        // try load tasks on startup (if already logged in)
        scope.launch {
            try {
                loadTasks()
            } catch (_: Throwable) {
                // probably not logged in yet, ignore
            }
        }
    }

    private fun handleLogin(username: String) {
        if (username.isBlank()) {
            view.showStatus("Enter a username")
            return
        }

        scope.launch {
            try {
                view.showStatus("Logging in…")
                api.login(username)
                view.setLoggedInUser(username)
                view.showStatus("Logged in as $username")
                loadTasks()
            } catch (e: Throwable) {
                view.showStatus("Login error: ${e.message}")
            }
        }
    }

    private fun handleCreateTask(
        title: String,
        description: String,
        deadlineRaw: String,
    ) {
        if (title.isBlank()) {
            view.showStatus("Enter a task title")
            return
        }

        val deadlineInstant = deadlineRaw.toInstantOrNull()

        scope.launch {
            try {
                view.showStatus("Creating task…")
                api.createTask(
                    title = title,
                    description = description,
                    deadline = deadlineInstant,
                )
                view.clearTaskInputs()
                view.showStatus("Task created")
                loadTasks()
            } catch (e: Throwable) {
                view.showStatus("Create error: ${e.message}")
            }
        }
    }

    private fun handleDeleteTask(taskId: String) {
        scope.launch {
            try {
                view.showStatus("Deleting task…")
                api.deleteTask(taskId)
                view.showStatus("Task deleted")
                loadTasks()
            } catch (e: Throwable) {
                view.showStatus("Delete error: ${e.message}")
            }
        }
    }

    private suspend fun loadTasks() {
        try {
            view.showStatus("Loading tasks…")
            val tasks = api.listTasks()
            currentTasks = tasks
            view.showTasks(tasks)
            view.showStatus("Loaded ${tasks.size} task(s)")
        } catch (e: Throwable) {
            view.showStatus("Error loading tasks: ${e.message}")
        }
    }

    private fun handleEditTask(
        taskId: String,
        title: String,
        description: String,
        deadlineRaw: String,
    ) {
        val deadlineInstant = deadlineRaw.toInstantOrNull() // same helper you used for create

        scope.launch {
            try {
                view.showStatus("Updating task…")
                api.updateTask(taskId, title, description, deadlineInstant)
                view.hideTaskDetails()
                loadTasks()
                view.showStatus("Task updated")
            } catch (e: Throwable) {
                view.showStatus("Update error: ${e.message}")
            }
        }
    }
}
