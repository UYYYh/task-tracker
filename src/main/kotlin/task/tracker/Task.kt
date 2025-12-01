package com.example.task.tracker

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

class Task(
    var title: String = "Untitled Task",
    var description: String = "",
    private val creationTime: TaskTime.Actual,
    private var deadline: TaskTime = TaskTime.NoDeadLine,
    private var completionTime: TaskTime = TaskTime.NotYetSet,
) {
    val id: UUID = UUID.randomUUID()

    init {
        require(
            deadline == TaskTime.NoDeadLine ||
                deadline == TaskTime.NotYetSet ||
                deadline as TaskTime.Actual > creationTime,
        )
    }

    companion object {
        fun simpleTask(title: String) =
            Task(
                title = title,
                creationTime = TaskTime.Actual(Clock.System.now()),
            )
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
