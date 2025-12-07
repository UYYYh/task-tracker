package com.example.task.tracker.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import task.tracker.app.TaskID

internal class Task(
    title: String = "Untitled Task",
    description: String = "",
    creationInstant: Instant = Clock.System.now(),
    deadline: Instant? = null,
    completionTime: Instant? = null,
) {
    val id: TaskID = TaskID.new()

    var title: String = title
        private set

    var description: String = description
        private set

    private val creationState: TaskTime.Actual = TaskTime.Actual(creationInstant)

    private var deadlineState: TaskTime =
        deadline?.let { TaskTime.Actual(it) } ?: TaskTime.NoDeadLine

    private var completionState: TaskTime =
        completionTime?.let { TaskTime.Actual(it) } ?: TaskTime.NotYetSet

    // Read-only “view” properties
    val creationTime: Instant
        get() = creationState.instant

    val deadline: Instant?
        get() = (deadlineState as? TaskTime.Actual)?.instant

    val completionTime: Instant?
        get() = (completionState as? TaskTime.Actual)?.instant

    val isCompleted: Boolean
        get() = completionState is TaskTime.Actual

    val hasDeadline: Boolean
        get() = deadlineState is TaskTime.Actual

    val isOverdue: Boolean
        get() =
            deadlineState is TaskTime.Actual &&
                (deadlineState as TaskTime.Actual).instant < Clock.System.now()

    init {
        if (deadline != null) {
            require(deadline > creationInstant) {
                "Deadline must be after creation time"
            }
        }
        if (completionTime != null) {
            require(completionTime >= creationInstant) {
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
        complete(null)
    }

    // completionTime can be null, in which case the current time is used
    // The task also does not have to be incomplete, the completion time can
    // be set even if the task is already completed
    fun complete(completionTime: Instant?) {
        val completionTime = completionTime ?: Clock.System.now()
        if (completionTime < creationTime) {
            throw IllegalArgumentException("Completion time must be after creation time")
        }
        this.completionState = TaskTime.Actual(completionTime)
    }

    fun uncomplete() {
        if (!isCompleted) {
            throw IllegalStateException("Task is not completed")
        }
        this.completionState = TaskTime.NotYetSet
    }

    fun setDeadline(deadline: Instant?) {
        this.deadlineState =
            if (deadline == null) {
                TaskTime.NoDeadLine
            } else {
                if (deadline < creationTime) {
                    throw IllegalArgumentException("Deadline must be after creation time")
                }
                TaskTime.Actual(deadline)
            }
    }

    fun update(
        newTitle: String,
        newDescription: String,
        newDeadline: Instant?,
        newCompletionTime: Instant?, // null => "mark as not completed"
    ) {
        // Snapshot old state
        val oldTitle = this.title
        val oldDescription = this.description
        val oldDeadline = this.deadline
        val oldCompletionState = this.completionState

        try {
            // Apply new basic fields
            setTitle(newTitle)
            setDescription(newDescription)
            setDeadline(newDeadline)

            // Apply completion state
            if (newCompletionTime == null) {
                // Caller wants "not completed"
                if (isCompleted) {
                    uncomplete()
                }
            } else {
                // Caller wants "completed at this time (or now if null inside complete)"
                complete(newCompletionTime)
            }
        } catch (e: Exception) {
            // Rollback everything atomically
            this.title = oldTitle
            this.description = oldDescription
            setDeadline(oldDeadline)
            this.completionState = oldCompletionState
            throw e
        }
    }

    override fun toString(): String =
        "{ Title: '$title' \n" +
            "Description='$description' \n" +
            "creationTime=$creationTime \n" +
            "deadline=$deadline \n" +
            "completionTime=$completionTime }"

    fun copy(): Task = Task(title, description, creationTime, deadline, completionTime)
}
