package com.example.task.tracker

import com.example.task.tracker.domain.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

internal fun exampleTasks(): List<Task> {
    val now: Instant = Clock.System.now()
    val tasks: MutableList<Task> = mutableListOf()
    tasks.add(Task.simpleTask("Buy milk"))
    tasks.add(
        Task(
            title = "Clean the kitchen",
            description = "Take out the trash",
            creationInstant = now,
            deadline = now + 1.days,
        ),
    )
    tasks.add(
        Task(
            title = "Finish report",
            description = "Complete the quarterly report",
            creationInstant = now - 2.days,
            deadline = now + 3.days,
        ),
    )
    return tasks
}

fun main() {
    println(exampleTasks())
}
