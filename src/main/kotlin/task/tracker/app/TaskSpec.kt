package task.tracker.app

import kotlinx.datetime.Instant

data class TaskSpec(
    val title: String,
    val description: String = "",
    val deadline: Instant? = null,
)
