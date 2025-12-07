package view
import kotlinx.browser.document
import model.TaskDTO
import org.w3c.dom.*
import util.formatPretty

class TaskListDomView private constructor(
    private val usernameInput: HTMLInputElement,
    private val loginBtn: HTMLButtonElement,
    private val statusDiv: HTMLElement,
    private val titleInput: HTMLInputElement,
    private val descriptionInput: HTMLInputElement,
    private val deadlineInput: HTMLInputElement,
    private val createBtn: HTMLButtonElement,
    private val tasksDiv: HTMLElement,
    private val loggedInUser: HTMLElement,
) : TaskListView {
    companion object {
        fun fromDocument(): TaskListDomView {
            val usernameInput = document.getElementById("username") as HTMLInputElement
            val loginBtn = document.getElementById("login-btn") as HTMLButtonElement
            val statusDiv = document.getElementById("status") as HTMLElement

            val titleInput = document.getElementById("task-title") as HTMLInputElement
            val descriptionInput = document.getElementById("task-description") as HTMLInputElement
            val deadlineInput = document.getElementById("task-deadline") as HTMLInputElement
            val createBtn = document.getElementById("create-btn") as HTMLButtonElement
            val tasksDiv = document.getElementById("tasks") as HTMLElement

            val loggedInUser = document.getElementById("logged-in-user") as HTMLElement

            return TaskListDomView(
                usernameInput,
                loginBtn,
                statusDiv,
                titleInput,
                descriptionInput,
                deadlineInput,
                createBtn,
                tasksDiv,
                loggedInUser,
            )
        }
    }

    // Events (Presenter sets these)
    override var onLoginClicked: ((String) -> Unit)? = null
    override var onCreateTaskClicked: ((String, String, String) -> Unit)? = null
    override var onDeleteTaskClicked: ((String) -> Unit)? = null

    init {
        loginBtn.onclick = {
            val username = usernameInput.value.trim()
            onLoginClicked?.invoke(username)
        }

        createBtn.onclick = {
            val title = titleInput.value.trim()
            val description = descriptionInput.value.trim()
            val deadlineRaw = deadlineInput.value.trim() // "2025-12-10T14:30" or ""
            onCreateTaskClicked?.invoke(title, description, deadlineRaw)
        }
    }

    // Presenter → View implementations

    override fun showStatus(message: String) {
        statusDiv.textContent = message
    }

    override fun setLoggedInUser(username: String?) {
        loggedInUser.textContent = username?.let { "Logged in as $it" } ?: ""
    }

    override fun clearTaskInputs() {
        titleInput.value = ""
        descriptionInput.value = ""
        deadlineInput.value = ""
    }

    override fun showTasks(tasks: List<TaskDTO>) {
        tasksDiv.innerHTML = ""

        if (tasks.isEmpty()) {
            tasksDiv.textContent = "(no tasks yet)"
            return
        }

        for (t in tasks) {
            val taskEl = document.createElement("div") as HTMLElement
            taskEl.className = "task-item"

            val textEl = document.createElement("pre") as HTMLElement
            textEl.textContent =
                """
                • ${t.title}
                  created: ${t.creationTime.formatPretty()}
                  due:     ${t.deadline?.formatPretty() ?: "No deadline"}
                  description: ${t.description}
                """.trimIndent()

            val deleteBtn = document.createElement("button") as HTMLButtonElement
            deleteBtn.textContent = "Delete"
            deleteBtn.onclick = {
                onDeleteTaskClicked?.invoke(t.id)
            }

            taskEl.appendChild(textEl)
            taskEl.appendChild(deleteBtn)
            tasksDiv.appendChild(taskEl)
        }
    }
}
