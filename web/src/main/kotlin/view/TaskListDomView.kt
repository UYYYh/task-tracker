package view

import kotlinx.browser.document
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import model.TaskDTO
import org.w3c.dom.*
import org.w3c.dom.events.Event
import util.toDatetimeLocalString

private const val TIMELINE_START_OFFSET_DAYS: Int = -2 // e.g. -3 to start 3 days before earliest
private const val TIMELINE_LENGTH_DAYS: Int = 14

class TaskListDomView(
    private val usernameInput: HTMLInputElement,
    private val loginBtn: HTMLButtonElement,
    private val statusDiv: HTMLElement,
    private val titleInput: HTMLInputElement,
    private val descriptionInput: HTMLInputElement,
    private val deadlineInput: HTMLInputElement,
    private val createBtn: HTMLButtonElement,
    private val timelineDiv: HTMLElement,
    private val loggedInUser: HTMLElement,
    private val modalRoot: HTMLElement,
    private val modalTitle: HTMLInputElement,
    private val modalDescription: HTMLTextAreaElement,
    private val modalDeadline: HTMLInputElement,
    private val modalSave: HTMLButtonElement,
    private val modalCancel: HTMLButtonElement,
    private val modalDelete: HTMLButtonElement,
) : TaskListView {
    // ========= companion: safe DOM wiring =========

    companion object {
        private fun input(id: String): HTMLInputElement {
            val el =
                document.getElementById(id)
                    ?: error("No element with id='$id' found in HTML")
            if (el !is HTMLInputElement) {
                error("Element with id='$id' is <${el.tagName}> but expected <input>")
            }
            return el
        }

        private fun button(id: String): HTMLButtonElement {
            val el =
                document.getElementById(id)
                    ?: error("No element with id='$id' found in HTML")
            if (el !is HTMLButtonElement) {
                error("Element with id='$id' is <${el.tagName}> but expected <button>")
            }
            return el
        }

        private fun block(id: String): HTMLElement {
            val el =
                document.getElementById(id)
                    ?: error("No element with id='$id' found in HTML")
            if (el !is HTMLElement) {
                error("Element with id='$id' is not an HTMLElement (tag=${el.tagName})")
            }
            return el
        }

        fun fromDocument(): TaskListDomView {
            val usernameInput = input("username")
            val loginBtn = button("login-btn")
            val statusDiv = block("status")

            val titleInput = input("task-title")
            val descriptionInput = input("task-description")
            val deadlineInput = input("task-deadline")
            val createBtn = button("create-btn")

            val timelineDiv = block("timeline")
            val loggedInUser = block("logged-in-user")

            val modalRoot = block("task-modal")
            val modalTitle = block("modal-title") as HTMLInputElement
            val modalDescription = block("modal-description") as HTMLTextAreaElement
            val modalDeadline = block("modal-deadline") as HTMLInputElement
            val modalSave = button("modal-save")
            val modalCancel = button("modal-cancel")
            val modalDelete = button("modal-delete")

            return TaskListDomView(
                usernameInput = usernameInput,
                loginBtn = loginBtn,
                statusDiv = statusDiv,
                titleInput = titleInput,
                descriptionInput = descriptionInput,
                deadlineInput = deadlineInput,
                createBtn = createBtn,
                timelineDiv = timelineDiv,
                loggedInUser = loggedInUser,
                modalRoot = modalRoot,
                modalTitle = modalTitle,
                modalDescription = modalDescription,
                modalDeadline = modalDeadline,
                modalSave = modalSave,
                modalCancel = modalCancel,
                modalDelete = modalDelete,
            )
        }

        private val userZone = TimeZone.currentSystemDefault()

        private fun Instant.toLocalDate(): LocalDate = this.toLocalDateTime(userZone).date
    }

    // ========= View -> Presenter callbacks =========

    override var onLoginClicked: ((String) -> Unit)? = null
    override var onCreateTaskClicked: ((String, String, String) -> Unit)? = null
    override var onDeleteTaskClicked: ((String) -> Unit)? = null
    override var onTaskClicked: ((String) -> Unit)? = null
    override var onTaskEditConfirmed: ((String, String, String, String) -> Unit)? = null

    private var currentTaskId: String? = null

    init {
        loginBtn.onclick = {
            val username = usernameInput.value.trim()
            onLoginClicked?.invoke(username)
        }

        createBtn.onclick = {
            val title = titleInput.value.trim()
            val description = descriptionInput.value.trim()
            val deadlineRaw = deadlineInput.value.trim()
            onCreateTaskClicked?.invoke(title, description, deadlineRaw)
        }

        modalCancel.onclick = {
            hideTaskDetails()
        }

        modalSave.onclick = { _: Event ->
            val id = currentTaskId
            if (id != null) {
                val title = modalTitle.value.trim()
                val description = modalDescription.value.trim()
                val deadlineRaw = modalDeadline.value.trim()

                onTaskEditConfirmed?.invoke(id, title, description, deadlineRaw)
            }
        }

        modalDelete.onclick = {
            val id = currentTaskId
            if (id != null) {
                onDeleteTaskClicked?.invoke(id)
            }
            hideTaskDetails()
        }
    }

    // ========= Presenter -> View methods =========

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

    override fun showTaskDetails(task: TaskDTO) {
        currentTaskId = task.id
        modalTitle.value = task.title
        modalDescription.value = task.description
        modalDeadline.value = task.deadline?.toDatetimeLocalString() ?: ""
        modalRoot.classList.remove("hidden")
    }

    override fun hideTaskDetails() {
        modalRoot.classList.add("hidden")
        currentTaskId = null
    }

    override fun showTasks(tasks: List<TaskDTO>) {
        renderTimeline(tasks)
    }

    // ========= timeline rendering =========

    private fun renderTimeline(
        tasks: List<TaskDTO>,
        offsetFromToday: Int = TIMELINE_START_OFFSET_DAYS,
        numDays: Int = TIMELINE_LENGTH_DAYS,
    ) {
        timelineDiv.innerHTML = ""
        if (tasks.isEmpty()) return

        // 1. per-task date ranges
        val ranges: List<Pair<LocalDate, LocalDate>> =
            tasks.map { t ->
                val start = t.creationTime.toLocalDate()
                val end = (t.deadline ?: t.creationTime).toLocalDate()
                val safeEnd = if (end < start) start else end
                start to safeEnd
            }

        // 2. fixed visible window
        val earliestStart = ranges.minBy { it.first }.first
        val firstDay = earliestStart + DatePeriod(days = offsetFromToday)
        val lastDay = firstDay + DatePeriod(days = numDays - 1)

        // 3. compute "today" column (if today is inside the window)
        val today =
            Clock.System
                .now()
                .toLocalDateTime(userZone)
                .date
        val todayIndex: Int? =
            if (today < firstDay || today > lastDay) {
                null
            } else {
                firstDay.daysUntil(today)
            }

        timelineDiv.style.position = "relative"

        if (todayIndex != null) {
            val todayBar = document.createElement("div") as HTMLElement
            todayBar.className = "timeline-today-bar"
            todayBar.style.position = "absolute"
            todayBar.style.top = "0"
            todayBar.style.bottom = "0"

            val percentWidth = 100.0 / numDays
            val left = percentWidth * todayIndex

            todayBar.style.left = "$left%"
            todayBar.style.width = "$percentWidth%"
            todayBar.style.zIndex = "0"

            timelineDiv.appendChild(todayBar)
        }

        // 5. header row (dates)
        val header = document.createElement("div") as HTMLElement
        header.className = "timeline-header"
        header.style.setProperty("display", "grid")
        header.style.setProperty("grid-template-columns", "repeat($numDays, 1fr)")
        header.style.position = "relative"
        header.style.zIndex = "1"

        for (i in 0 until numDays) {
            val day = firstDay + DatePeriod(days = i)
            val cell = document.createElement("div") as HTMLElement
            cell.className = "timeline-day"
            cell.textContent = "${day.monthNumber}/${day.dayOfMonth}"

            // vertical separators between days
            cell.style.setProperty("border-right", "1px solid #555")

            header.appendChild(cell)
        }

        // 6. tasks row
        val row = document.createElement("div") as HTMLElement
        row.className = "timeline-row"
        row.style.setProperty("display", "grid")
        row.style.setProperty("grid-template-columns", "repeat($numDays, 1fr)")
        row.style.position = "relative"
        row.style.zIndex = "1"

        tasks.forEachIndexed { idx, t ->
            val (start, end) = ranges[idx]

            // skip tasks outside visible window
            if (end < firstDay || start > lastDay) return@forEachIndexed

            val clampedStart = if (start < firstDay) firstDay else start
            val clampedEnd = if (end > lastDay) lastDay else end

            val startIdx = firstDay.daysUntil(clampedStart)
            val endIdx = firstDay.daysUntil(clampedEnd)

            val card = document.createElement("div") as HTMLElement
            card.className = "timeline-task-card"
            card.textContent = t.title
            card.style.setProperty("grid-column", "${startIdx + 1} / ${endIdx + 2}")

            card.onclick = {
                onTaskClicked?.invoke(t.id)
            }

            row.appendChild(card)
        }

        timelineDiv.appendChild(header)
        timelineDiv.appendChild(row)
    }
}
