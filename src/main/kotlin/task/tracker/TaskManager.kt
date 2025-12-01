package com.example.task.tracker

import com.example.task.tracker.domain.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

class TaskManager {
    private var tasks: MutableMap<UUID, Task> = mutableMapOf()

    fun createTask(
        title: String,
        description: String = "",
        creationTime: Instant = Clock.System.now(),
        completionTime: Instant? = null,
        deadline: Instant? = null,
    ): UUID {
        val task =
            Task(
                title = title,
                description = description,
                creationTime = creationTime,
                deadline = deadline,
                completionTime = completionTime,
            )
        tasks[task.id] = task
        return task.id
    }

    fun deleteTask(id: UUID): Boolean = tasks.remove(id) != null

    fun completeTask(id: UUID): Boolean {
        val task = tasks[id] ?: return false
        task.complete()
        return true
    }

    fun setDeadline(
        id: UUID,
        deadline: Instant,
    ): Boolean {
        val task = tasks[id] ?: return false
        task.setDeadline(deadline)
        return true
    }

    fun setDescription(
        id: UUID,
        description: String,
    ): Boolean {
        val task = tasks[id] ?: return false
        task.setDescription(description)
        return true
    }

    fun setTitle(
        id: UUID,
        title: String,
    ): Boolean {
        val task = tasks[id] ?: return false
        task.setTitle(title)
        return true
    }

    override fun toString(): String = tasks.toString()
}
