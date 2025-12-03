package task.tracker.app

import com.example.task.tracker.dto.TaskDTO
import kotlinx.datetime.Instant
import task.tracker.app.TaskSpec

interface UserTaskManager {
    fun createTask(spec: TaskSpec): TaskID

    fun deleteTask(taskID: TaskID): Boolean

    fun renameTask(
        taskID: TaskID,
        newTitle: String,
    ): Boolean

    fun changeDescription(
        taskID: TaskID,
        newDescription: String,
    ): Boolean

    fun setDeadline(
        taskID: TaskID,
        deadline: Instant,
    ): Boolean

    fun completeTask(
        taskID: TaskID,
        at: Instant? = null,
    ): Boolean

    fun getTask(taskID: TaskID): TaskDTO?

    fun listTasks(): List<TaskDTO>
}
