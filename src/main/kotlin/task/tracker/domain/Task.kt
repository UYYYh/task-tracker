package com.example.task.tracker.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

class Task(
    private var title: String = "Untitled Task",
    private var description: String = "",
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

    fun setTitle(title: String) {
        this.title = title
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun complete() {
        complete(completionTime = Clock.System.now())
    }

    fun complete(completionTime: Instant) {
        if (completionTime < creationTime.instant) {
            throw IllegalArgumentException("Completion time must be after creation time")
        }
        this.completionTime = TaskTime.Actual(completionTime)
    }

    fun setDeadline(deadline: Instant) {
        if (deadline < creationTime.instant) {
            throw IllegalArgumentException("Deadline must be after creation time")
        }
        this.deadline = TaskTime.Actual(deadline)
    }

    fun isCompleted(): Boolean = completionTime is TaskTime.Actual

    fun isOverdue(): Boolean =
        deadline is TaskTime.Actual &&
            (deadline as TaskTime.Actual).instant < Clock.System.now()

    fun hasDeadline(): Boolean = deadline is TaskTime.Actual

    fun getDescription(): String = description

    fun getCreationTime(): Instant = creationTime.instant

    fun getDeadline(): Instant? = (deadline as? TaskTime.Actual)?.instant

    fun getCompletionTime(): Instant? = (completionTime as? TaskTime.Actual)?.instant

    fun getTitle(): String = title

    override fun toString(): String =
        "{ Title: '$title' \n" +
            "Description='$description' \n" +
            "creationTime=$creationTime \n" +
            "deadline=$deadline \n" +
            "completionTime=$completionTime }"
}
