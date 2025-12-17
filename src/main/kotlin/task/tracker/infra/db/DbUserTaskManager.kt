package com.example.task.tracker.infra.db

import com.example.task.tracker.dto.TaskDTO
import kotlinx.datetime.Instant
import task.tracker.app.TaskID
import task.tracker.app.TaskSpec
import task.tracker.app.UserID
import task.tracker.app.UserTaskManager
import java.sql.Connection
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.sql.DataSource

private fun ResultSet.toTaskDTO(): TaskDTO =
    TaskDTO(
        id = getObject("id", UUID::class.java).toString(),
        title = getString("title"),
        description = getString("description"),
        creationTime =
            getObject(
                "created_at",
                OffsetDateTime::class.java,
            ).toInstant().let { Instant.fromEpochMilliseconds(it.toEpochMilli()) },
        deadline =
            getObject(
                "deadline",
                OffsetDateTime::class.java,
            )?.toInstant()?.let { Instant.fromEpochMilliseconds(it.toEpochMilli()) },
        completionTime =
            getObject(
                "completed_at",
                OffsetDateTime::class.java,
            )?.toInstant()?.let { Instant.fromEpochMilliseconds(it.toEpochMilli()) },
    )

private fun Instant.toOffsetDateTime(): OffsetDateTime =
    OffsetDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(this.toEpochMilliseconds()),
        ZoneOffset.UTC,
    )

private fun OffsetDateTime.toKxInstant(): Instant = Instant.fromEpochMilliseconds(this.toInstant().toEpochMilli())

class DbUserTaskManager(
    private val userID: UserID,
    private val dataSource: DataSource,
) : UserTaskManager {
    override fun createTask(spec: TaskSpec): TaskDTO {
        val sql =
            """
            INSERT INTO tasks (user_id, title, description, deadline)
            VALUES (?, ?, ?, ?)
            RETURNING id, title, description, created_at, deadline, completed_at
            """.trimIndent()

        return dataSource.withConnection { c ->
            c.prep(
                sql,
                bind = {
                    setObject(1, userID.raw)
                    setString(2, spec.title)
                    setString(3, spec.description)
                    setObject(4, spec.deadline?.toOffsetDateTime())
                },
                read = {
                    executeQuery().use { rs ->
                        rs.next()
                        rs.toTaskDTO()
                    }
                },
            )
        }
    }

    override fun deleteTask(taskID: TaskID): Boolean {
        TODO("Not yet implemented")
    }

    override fun renameTask(
        taskID: TaskID,
        newTitle: String,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun changeDescription(
        taskID: TaskID,
        newDescription: String,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun setDeadline(
        taskID: TaskID,
        deadline: Instant?,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun completeTask(
        taskID: TaskID,
        at: Instant?,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun uncompleteTask(taskID: TaskID): Boolean {
        TODO("Not yet implemented")
    }

    override fun getTask(taskID: TaskID): TaskDTO? {
        TODO("Not yet implemented")
    }

    override fun listTasks(): List<TaskDTO> {
        TODO("Not yet implemented")
    }

    override fun updateTask(
        taskID: TaskID,
        title: String,
        description: String,
        deadline: Instant?,
        completionTime: Instant?,
    ): Boolean {
        TODO("Not yet implemented")
    }
}
