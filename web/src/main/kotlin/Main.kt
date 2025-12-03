import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement

private val scope = MainScope()

fun main() {
    val root = document.getElementById("app") as HTMLDivElement
    renderLogin(root)
}

private fun renderLogin(root: HTMLDivElement) {
    root.innerHTML =
        """
        <h1>Task Tracker</h1>
        <label>
          Username:
          <input id="username" />
        </label>
        <button id="login-btn">Login</button>
        <pre id="status"></pre>
        <div id="tasks"></div>
        """.trimIndent()

    val usernameInput = document.getElementById("username") as HTMLInputElement
    val loginBtn = document.getElementById("login-btn") as HTMLButtonElement
    val status = document.getElementById("status") as HTMLDivElement
    val tasksDiv = document.getElementById("tasks") as HTMLDivElement

    loginBtn.onclick = {
        val username = usernameInput.value.trim()
        if (username.isEmpty()) {
            status.textContent = "Enter a username"
        }

        scope.launch {
            try {
                status.textContent = "Logging in..."
                val token = login(username)

                // Save token so we can reuse later
                window.localStorage.setItem("authToken", token)

                status.textContent = "Logged in as $username"
                loadAndRenderTasks(tasksDiv, token)
            } catch (e: Throwable) {
                status.textContent = "Error: ${e.message}"
            }
        }
    }
}

private fun loadAndRenderTasks(
    target: HTMLDivElement,
    token: String,
) {
    scope.launch {
        try {
            val tasks = fetchTasks(token)
            if (tasks.isEmpty()) {
                target.innerHTML = "<p>No tasks yet.</p>"
                return@launch
            }

            val html =
                buildString {
                    append("<h2>Your tasks</h2><ul>")
                    for (t in tasks) {
                        append("<li>${t.title} – ${t.description}</li>")
                    }
                    append("</ul>")
                }
            target.innerHTML = html
        } catch (e: Throwable) {
            target.innerHTML = "<p>Error loading tasks: ${e.message}</p>"
        }
    }
}
