package com.example.task.tracker

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

class Task(
    var title: String = "Untitled Task",
    var description: String = "",
    creationTime: Instant = Clock.System.now(),
    deadline: Instant? = null,
    completionTime: Instant? = null,
) {
    val id: UUID = UUID.randomUUID()
    private val creationTime: TaskTime.Actual = TaskTime.Actual(creationTime)
    private var deadline: TaskTime =
        when (deadline) {
            null -> TaskTime.NoDeadLine
            else -> TaskTime.Actual(deadline)
        }
    private var completionTime: TaskTime =
        when (completionTime) {
            null -> TaskTime.NotYetSet
            else -> TaskTime.Actual(completionTime)
        }

    init {
        if (deadline != null) {
            require(deadline > creationTime) {
                "Deadline must be after creation time"
            }
        }
        if (completionTime != null) {
            require(completionTime >= creationTime) {
                "Completion time must be after creation time"
            }
        }
    }

    companion object {
        fun simpleTask(title: String) = Task(title = title)
    }

    fun isCompleted(): Boolean = completionTime is TaskTime.Actual

    fun complete(completionTime: Instant) {
        this.completionTime = TaskTime.Actual(completionTime)
    }

    fun complete() {
        complete(completionTime = Clock.System.now())
    }

    fun isOverdue(): Boolean =
        deadline is TaskTime.Actual &&
            (deadline as TaskTime.Actual).instant < Clock.System.now()

    fun setDeadline(deadline: Instant) {
        this.deadline = TaskTime.Actual(deadline)
    }

    fun getCreationTime(): Instant = creationTime.instant

    fun getDeadline(): Instant? = (deadline as? TaskTime.Actual)?.instant

    fun getCompletionTime(): Instant? = (completionTime as? TaskTime.Actual)?.instant

    override fun toString(): String =
        "{ Title: '$title' \n" +
            "Description='$description' \n" +
            "creationTime=$creationTime \n" +
            "deadline=$deadline \n" +
            "completionTime=$completionTime }"
}
