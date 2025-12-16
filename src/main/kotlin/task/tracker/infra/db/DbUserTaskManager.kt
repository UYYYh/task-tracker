package com.example.task.tracker.infra.db

import com.example.task.tracker.dto.TaskDTO
import kotlinx.datetime.Instant
import task.tracker.app.TaskID
import task.tracker.app.TaskSpec
import task.tracker.app.UserTaskManager

class DbUserTaskManager : UserTaskManager {
    override fun createTask(spec: TaskSpec): TaskDTO {
        TODO("Not yet implemented")
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
