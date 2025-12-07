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
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import util.formatPretty
import util.toDatetimeLocalString

private const val TIMELINE_START_OFFSET_DAYS: Int = -1 // e.g. -3 to start 3 days before earliest
private const val TIMELINE_LENGTH_DAYS: Int = 14

class TaskListDomView(
    private val usernameInput: HTMLInputElement,
    private val loginBtn: HTMLButtonElement,
    private val statusDiv: HTMLElement,
    private val titleInput: HTMLInputElement,
    private val descriptionInput: HTMLTextAreaElement,
    private val deadlineInput: HTMLInputElement,
    private val createBtn: HTMLButtonElement,
    private val timelineDiv: HTMLElement,
    private val loggedInUser: HTMLElement,
    private val modalRoot: HTMLElement,
    private val modalTitle: HTMLInputElement,
    private val modalDescription: HTMLTextAreaElement,
    private val modalDeadline: HTMLInputElement,
    private val modalCompleted: HTMLInputElement,
    private val modalCreated: HTMLElement,
    private val modalStatus: HTMLElement,
    private val modalSave: HTMLButtonElement,
    private val modalCancel: HTMLButtonElement,
    private val modalDelete: HTMLButtonElement,
    private val modalToggleComplete: HTMLButtonElement,
    private val loginModalRoot: HTMLElement,
    private val loginCancelBtn: HTMLButtonElement,
    private val userIconToggleBtn: HTMLButtonElement,
    private val createTaskModalRoot: HTMLElement,
    private val createCancelBtn: HTMLButtonElement,
    private val addTaskToggleBtn: HTMLButtonElement,
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

        private fun textarea(id: String): HTMLTextAreaElement {
            val el =
                document.getElementById(id)
                    ?: error("No element with id='$id' found in HTML")
            if (el !is HTMLTextAreaElement) {
                error("Element with id='$id' is <${el.tagName}> but expected <textarea>")
            }
            return el
        }

        fun fromDocument(): TaskListDomView {
            val usernameInput = input("username")
            val loginBtn = button("login-btn")
            val statusDiv = block("status")

            val titleInput = input("task-title")
            val descriptionInput = textarea("task-description")
            val deadlineInput = input("task-deadline")
            val createBtn = button("create-btn")

            val timelineDiv = block("timeline")
            val loggedInUser = block("logged-in-user")

            val modalRoot = block("task-modal")
            val modalTitle = block("modal-title") as HTMLInputElement
            val modalDescription = block("modal-description") as HTMLTextAreaElement
            val modalDeadline = block("modal-deadline") as HTMLInputElement
            val modalCompleted = block("modal-completed") as HTMLInputElement
            val modalCreated = block("modal-created")
            val modalStatus = block("modal-status")
            val modalSave = button("modal-save")
            val modalCancel = button("modal-cancel")
            val modalDelete = button("modal-delete")
            val modalToggleComplete = button("modal-toggle-complete")

            val loginModalRoot = block("login-modal")
            val loginCancelBtn = button("login-cancel")
            val userIconToggleBtn = button("user-icon-toggle")

            val createTaskModalRoot = block("create-task-modal")
            val createCancelBtn = button("create-cancel")
            val addTaskToggleBtn = button("add-task-toggle")

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
                modalCreated = modalCreated,
                modalStatus = modalStatus,
                modalToggleComplete = modalToggleComplete,
                modalCompleted = modalCompleted,
                loginModalRoot = loginModalRoot,
                loginCancelBtn = loginCancelBtn,
                userIconToggleBtn = userIconToggleBtn,
                createTaskModalRoot = createTaskModalRoot,
                createCancelBtn = createCancelBtn,
                addTaskToggleBtn = addTaskToggleBtn,
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
    override var onTaskEditConfirmed: ((String, String, String, String, String) -> Unit)? = null
    override var onToggleCompletionClicked: ((taskId: String, makeCompleted: Boolean) -> Unit)? = null

    private var currentTaskId: String? = null
    private var currentIsCompleted = false

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
                val completionTimeRaw = modalCompleted.value.trim()

                onTaskEditConfirmed?.invoke(id, title, description, deadlineRaw, completionTimeRaw)
            }
        }

        modalDelete.onclick = {
            val id = currentTaskId
            if (id != null) {
                onDeleteTaskClicked?.invoke(id)
            }
            hideTaskDetails()
        }

        modalToggleComplete.onclick = {
            val id = currentTaskId
            if (id != null) {
                onToggleCompletionClicked?.invoke(id, !currentIsCompleted)
            }
            hideTaskDetails()
        }

        userIconToggleBtn.onclick = {
            loginModalRoot.classList.remove("hidden")
            usernameInput.focus()
        }

        loginCancelBtn.onclick = {
            loginModalRoot.classList.add("hidden")
        }

        loginBtn.onclick = {
            val username = usernameInput.value.trim()
            onLoginClicked?.invoke(username)
            loginModalRoot.classList.add("hidden")
        }

        // open create-task modal
        addTaskToggleBtn.onclick = {
            createTaskModalRoot.classList.remove("hidden")
            titleInput.focus()
        }

        createCancelBtn.onclick = {
            createTaskModalRoot.classList.add("hidden")
        }

        createBtn.onclick = {
            val title = titleInput.value.trim()
            val description = descriptionInput.value.trim()
            val deadlineRaw = deadlineInput.value.trim()
            onCreateTaskClicked?.invoke(title, description, deadlineRaw)
            // close after submit; presenter will refresh tasks
            createTaskModalRoot.classList.add("hidden")
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

        modalCreated.textContent = task.creationTime.formatPretty() // or toDatetimeLocalString()
        modalTitle.value = task.title
        modalDescription.value = task.description
        modalDeadline.value = task.deadline?.toDatetimeLocalString() ?: ""
        modalCompleted.value = task.completionTime?.toDatetimeLocalString() ?: ""

        val now = Clock.System.now()

        val statusText =
            when {
                task.completionTime != null &&
                    task.deadline != null &&
                    task.completionTime > task.deadline ->
                    "Completed late"

                task.completionTime != null ->
                    "Completed"

                task.deadline != null && task.deadline < now ->
                    "Overdue"

                else ->
                    "Incomplete"
            }

        modalStatus.textContent = statusText
        currentIsCompleted = task.completionTime != null

        modalToggleComplete.textContent =
            if (currentIsCompleted) "Mark incomplete" else "Complete now"

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

    private data class TimelineWindow(
        val firstDay: LocalDate,
        val lastDay: LocalDate,
    )

    private fun computeRanges(tasks: List<TaskDTO>): List<Pair<LocalDate, LocalDate>> =
        tasks.map { t ->
            val start = t.creationTime.toLocalDate()
            val end = (t.deadline ?: t.creationTime).toLocalDate()
            val safeEnd = if (end < start) start else end
            start to safeEnd
        }

    private fun computeWindow(
        ranges: List<Pair<LocalDate, LocalDate>>,
        offsetFromToday: Int,
        numDays: Int,
    ): TimelineWindow {
        val earliestStart = ranges.minBy { it.first }.first
        val firstDay = earliestStart + DatePeriod(days = offsetFromToday)
        val lastDay = firstDay + DatePeriod(days = numDays - 1)
        return TimelineWindow(firstDay, lastDay)
    }

    private fun computeTodayIndex(window: TimelineWindow): Int? {
        val today =
            Clock.System
                .now()
                .toLocalDateTime(userZone)
                .date

        return if (today < window.firstDay || today > window.lastDay) {
            null
        } else {
            window.firstDay.daysUntil(today)
        }
    }

    private fun buildTodayBar(
        todayIndex: Int,
        numDays: Int,
    ): HTMLElement {
        val bar = document.createElement("div") as HTMLElement
        bar.className = "timeline-today-bar"
        bar.style.position = "absolute"
        bar.style.top = "0"
        bar.style.bottom = "0"

        val percentWidth = 100.0 / numDays
        val left = percentWidth * todayIndex

        bar.style.left = "$left%"
        bar.style.width = "$percentWidth%"
        bar.style.zIndex = "0"

        return bar
    }

    private fun buildTimelineHeader(
        window: TimelineWindow,
        numDays: Int,
    ): HTMLElement {
        val header = document.createElement("div") as HTMLElement
        header.className = "timeline-header"
        header.style.setProperty("display", "grid")
        header.style.setProperty("grid-template-columns", "repeat($numDays, 1fr)")
        header.style.position = "relative"
        header.style.zIndex = "1"

        for (i in 0 until numDays) {
            val day = window.firstDay + DatePeriod(days = i)
            val cell = document.createElement("div") as HTMLElement
            cell.className = "timeline-day"
            cell.textContent = "${day.monthNumber}/${day.dayOfMonth}"
            cell.style.setProperty("border-right", "1px solid #555")
            header.appendChild(cell)
        }

        return header
    }

    private fun HTMLElement.applyStatusClass(
        task: TaskDTO,
        now: Instant,
    ) {
        when {
            // completed late
            task.completionTime != null &&
                task.deadline != null &&
                task.completionTime > task.deadline ->
                this.classList.add("completed-late")

            // completed on time
            task.completionTime != null ->
                this.classList.add("completed")

            // incomplete + has deadline + overdue
            task.deadline != null && task.deadline < now ->
                this.classList.add("overdue")

            // incomplete + has deadline + not overdue
            task.deadline != null ->
                this.classList.add("incomplete")

            // incomplete + NO deadline
            else ->
                this.classList.add("no-deadline")
        }
    }

    private fun buildTimelineRow(
        tasks: List<TaskDTO>,
        ranges: List<Pair<LocalDate, LocalDate>>,
        window: TimelineWindow,
        numDays: Int,
        onTaskClicked: ((String) -> Unit)?,
    ): HTMLElement {
        val row = document.createElement("div") as HTMLElement
        row.className = "timeline-row"
        row.style.setProperty("display", "grid")
        row.style.setProperty("grid-template-columns", "repeat($numDays, 1fr)")
        row.style.position = "relative"
        row.style.zIndex = "1"

        val now = Clock.System.now()

        tasks.forEachIndexed { idx, t ->
            val (start, end) = ranges[idx]

            // skip tasks outside visible window
            if (end < window.firstDay || start > window.lastDay) return@forEachIndexed

            val clampedStart = if (start < window.firstDay) window.firstDay else start
            val clampedEnd = if (end > window.lastDay) window.lastDay else end

            val startIdx = window.firstDay.daysUntil(clampedStart)
            val endIdx = window.firstDay.daysUntil(clampedEnd)

            val card = document.createElement("div") as HTMLElement
            card.classList.add("timeline-task-card")
            card.applyStatusClass(t, now)
            card.textContent = t.title
            card.style.setProperty("grid-column", "${startIdx + 1} / ${endIdx + 2}")

            card.onclick = {
                onTaskClicked?.invoke(t.id)
            }

            row.appendChild(card)
        }

        return row
    }

    private fun buildTimelineLegend(): HTMLElement {
        val legend = document.createElement("div") as HTMLElement
        legend.className = "timeline-legend"

        fun addLegendItem(
            colorClass: String,
            label: String,
        ) {
            val item = document.createElement("div") as HTMLElement
            item.className = "timeline-legend-item"

            val swatch = document.createElement("span") as HTMLElement
            swatch.className = "legend-swatch $colorClass"

            val text = document.createElement("span") as HTMLElement
            text.textContent = label

            item.appendChild(swatch)
            item.appendChild(text)
            legend.appendChild(item)
        }

        addLegendItem("completed", "Completed on time")
        addLegendItem("completed-late", "Completed late")
        addLegendItem("overdue", "Overdue")
        addLegendItem("incomplete", "Incomplete (has deadline)")
        addLegendItem("no-deadline", "Incomplete (no deadline)")

        return legend
    }

    private fun renderTimeline(
        tasks: List<TaskDTO>,
        offsetFromToday: Int = TIMELINE_START_OFFSET_DAYS,
        numDays: Int = TIMELINE_LENGTH_DAYS,
    ) {
        timelineDiv.innerHTML = ""
        if (tasks.isEmpty()) return

        val ranges = computeRanges(tasks)
        val window = computeWindow(ranges, offsetFromToday, numDays)
        val todayIndex = computeTodayIndex(window)

        // legend at the top
        val legend = buildTimelineLegend()
        timelineDiv.appendChild(legend)

        // wrapper for header + row + today-bar
        val gridWrapper = document.createElement("div") as HTMLElement
        gridWrapper.className = "timeline-grid-wrapper"
        gridWrapper.style.position = "relative"

        // today bar lives INSIDE the wrapper
        if (todayIndex != null) {
            val todayBar = buildTodayBar(todayIndex, numDays)
            gridWrapper.appendChild(todayBar)
        }

        val header = buildTimelineHeader(window, numDays)
        val row = buildTimelineRow(tasks, ranges, window, numDays, onTaskClicked)

        gridWrapper.appendChild(header)
        gridWrapper.appendChild(row)

        timelineDiv.appendChild(gridWrapper)
    }
}
