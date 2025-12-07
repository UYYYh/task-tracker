import api.TaskApi
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import model.TaskDTO
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

private val scope = MainScope()

fun main() {
    // Grab DOM elements
    val usernameInput = document.getElementById("username") as HTMLInputElement
    val loginBtn = document.getElementById("login-btn") as HTMLButtonElement
    val statusDiv = document.getElementById("status") as HTMLElement

    val titleInput = document.getElementById("task-title") as HTMLInputElement
    val descriptionInput = document.getElementById("task-description") as HTMLInputElement
    val deadlineInput = document.getElementById("task-deadline") as HTMLInputElement
    val createBtn = document.getElementById("create-btn") as HTMLButtonElement
    val tasksDiv = document.getElementById("tasks") as HTMLElement

    val loggedInUser = document.getElementById("logged-in-user") as HTMLElement

    fun renderTasks(tasks: List<TaskDTO>) {
        if (tasks.isEmpty()) {
            tasksDiv.textContent = "(no tasks yet)"
            return
        }
        val sb = StringBuilder()
        for (t in tasks) {
            sb
                .append("• ")
                .append(
                    "${t.title}\n" +
                        "created: ${t.creationTime.formatPretty()}\n" +
                        "due:     ${t.deadline?.formatPretty() ?: "No deadline"}\n" +
                        "description: ${t.description}\n",
                )
            sb.append("\n")
        }
        tasksDiv.textContent = sb.toString()
    }

    fun showStatus(msg: String) {
        statusDiv.textContent = msg
    }

    fun loadTasks() {
        scope.launch {
            try {
                showStatus("Loading tasks…")
                val tasks = TaskApi.listTasks()
                renderTasks(tasks)
                showStatus("Loaded ${tasks.size} task(s)")
            } catch (e: Throwable) {
                showStatus("Error loading tasks: ${e.message}")
            }
        }
    }

    // Login button behaviour
    loginBtn.onclick = {
        val username = usernameInput.value.trim()
        if (username.isEmpty()) {
            showStatus("Enter a username")
        }

        scope.launch {
            try {
                showStatus("Logging in…")
                TaskApi.login(username)
                showStatus("Logged in as $username")
                loadTasks()
            } catch (e: Throwable) {
                showStatus("Login error: ${e.message}")
            }
        }
    }

    // Create task button behaviour
    createBtn.onclick = {
        val title = titleInput.value.trim()
        val description = descriptionInput.value.trim()
        if (title.isEmpty()) {
            showStatus("Enter a task title")
        }

        scope.launch {
            try {
                showStatus("Creating task…")
                TaskApi.createTask(title = title, description = description)
                titleInput.value = ""
                descriptionInput.value = ""
                showStatus("Task created")
                loadTasks()
            } catch (e: Throwable) {
                showStatus("Create error: ${e.message}")
            }
        }
    }

    // Optional: auto-load tasks if token already stored
    // (refresh page after previous login)
    scope.launch {
        try {
            loadTasks()
        } catch (_: Throwable) {
            // ignore if not logged in yet
        }
    }
}
