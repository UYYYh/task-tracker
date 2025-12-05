package view

import kotlinx.browser.document
import model.TaskDTO
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class TaskListDomView(
    private val root: HTMLElement,
) : TaskListView {
    override var onRefreshClicked: (() -> Unit)? = null

    override fun showLoading() {
        root.innerHTML = "<p>Loading tasks…</p>"
    }

    override fun showTasks(tasks: List<TaskDTO>) {
        root.innerHTML = "" // clear UI

        // Container div
        val container = document.createElement("div") as HTMLDivElement

        // Add each task
        tasks.forEach { task ->
            val taskDiv = document.createElement("div") as HTMLDivElement
            taskDiv.textContent = task.title
            taskDiv.style.padding = "4px"
            taskDiv.style.borderBottom = "1px solid #ddd"

            container.appendChild(taskDiv)
        }

        root.appendChild(container)

        // Refresh button
        val refreshBtn = document.createElement("button") as HTMLButtonElement
        refreshBtn.textContent = "Refresh"
        refreshBtn.onclick = {
            onRefreshClicked?.invoke()
        }

        root.appendChild(refreshBtn)
    }

    override fun showError(message: String) {
        root.innerHTML = "<p style='color:red'>$message</p>"
    }
}
