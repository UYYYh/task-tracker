package com.example

import com.example.task.tracker.dto.TaskDTO
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import task.tracker.app.TaskID
import task.tracker.app.TaskSpec
import task.tracker.app.UserTaskManager
import task.tracker.infra.memory.InMemoryTaskManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days

class InMemoryTaskManagerTest {
    private val taskService: InMemoryTaskManager = InMemoryTaskManager()
    private val userTaskManager: UserTaskManager = taskService.newUser()
    private val now: Instant = Clock.System.now()

    private fun arbitraryTaskSpec(): TaskSpec =
        TaskSpec(
            title = "Test Task",
            description = "This is a test task",
            deadline = null,
        )

    @Test
    fun testCreateAndDeleteTask() {
        assertEquals(userTaskManager.listTasks().size, 0)

        val task1ID: TaskID = TaskID(userTaskManager.createTask(arbitraryTaskSpec()).id)
        assertEquals(userTaskManager.listTasks().size, 1)

        val retrievedTask: TaskDTO? = userTaskManager.getTask(task1ID)

        assertNotNull(retrievedTask)

        assertEquals(retrievedTask.title, "Test Task")
        assertEquals(retrievedTask.description, "This is a test task")
        assertEquals(retrievedTask.deadline, null)

        userTaskManager.deleteTask(task1ID)
        assertEquals(userTaskManager.listTasks().size, 0)

        val deletedTask: TaskDTO? = userTaskManager.getTask(task1ID)
        assertNull(deletedTask)
    }

    @Test
    fun testRenameTask() {
        val task1ID: TaskID = TaskID(userTaskManager.createTask(arbitraryTaskSpec()).id)
        userTaskManager.renameTask(task1ID, "Renamed Task")

        val retrievedTask: TaskDTO? = userTaskManager.getTask(task1ID)

        assertNotNull(retrievedTask)
        assertEquals(retrievedTask.title, "Renamed Task")
    }

    @Test
    fun testChangeDescription() {
        val task1ID: TaskID = TaskID(userTaskManager.createTask(arbitraryTaskSpec()).id)
        userTaskManager.changeDescription(task1ID, "Changed description")

        val retrievedTask: TaskDTO? = userTaskManager.getTask(task1ID)

        assertNotNull(retrievedTask)
        assertEquals(retrievedTask.description, "Changed description")
    }

    @Test
    fun testSetDeadline() {
        val task1ID: TaskID = TaskID(userTaskManager.createTask(arbitraryTaskSpec()).id)
        userTaskManager.setDeadline(task1ID, now.plus(1.days))

        val retrievedTask: TaskDTO? = userTaskManager.getTask(task1ID)

        assertNotNull(retrievedTask)
        assertEquals(retrievedTask.deadline, now.plus(1.days))
    }

    @Test
    fun testCompleteTask() {
        val task1ID: TaskID = TaskID(userTaskManager.createTask(arbitraryTaskSpec()).id)
        userTaskManager.completeTask(task1ID)

        val retrievedTask: TaskDTO? = userTaskManager.getTask(task1ID)

        assertNotNull(retrievedTask)
        assertNotNull(retrievedTask.completionTime)
    }
}
