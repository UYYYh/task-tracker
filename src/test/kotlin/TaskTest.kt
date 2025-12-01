package com.example

import com.example.task.tracker.domain.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds

class TaskTest {
    private val arbitraryInstant: Instant =
        Instant.fromEpochSeconds(1625097600) // July 1, 2021 00:00:00 GMT
    private val now = Clock.System.now()
    private val arbitraryTask: Task =
        Task(
            title = "Arbitrary Task",
            description = "This is an arbitrary task",
            creationInstant = arbitraryInstant,
        )

    @Test
    fun testIllegalTaskCreation() {
        try {
            Task(
                title = "Invalid Task",
                description = "This task has an invalid deadline",
                creationInstant = arbitraryInstant,
                deadline = arbitraryInstant - 10.seconds,
            )
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Good: exception was thrown as expected.
        }
    }

    @Test
    fun testCompleteTaskWithInvalidTime() {
        try {
            arbitraryTask.complete(completionTime = arbitraryInstant - 5.seconds)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Good: exception was thrown as expected.
        }
    }

    @Test
    fun testValidTaskCreation() {
        Task(
            title = "Valid Task",
            description = "This task has a valid deadline",
            creationInstant = arbitraryInstant,
            deadline = arbitraryInstant + 10.seconds,
        )
    }

    @Test
    fun testCompleteTask() {
        assert(!arbitraryTask.isCompleted)
        arbitraryTask.complete()
        assert(arbitraryTask.isCompleted)
    }

    @Test
    fun testOverdue() {
        assert(!arbitraryTask.isOverdue)
        arbitraryTask.setDeadline(now - 1.seconds)
        assert(arbitraryTask.isOverdue)
    }
}
