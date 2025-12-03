package task.tracker.infra.memory

import com.example.task.tracker.domain.Task
import com.example.task.tracker.dto.TaskDTO
import com.example.task.tracker.dto.toDTO
import kotlinx.datetime.Instant
import task.tracker.app.TaskID
import task.tracker.app.TaskSpec
import task.tracker.app.UserTaskManager

class InMemoryUserTaskManager : UserTaskManager {
    private val tasks: MutableMap<TaskID, Task> = mutableMapOf()

    override fun createTask(spec: TaskSpec): TaskDTO {
        val task =
            Task(
                title = spec.title,
                description = spec.description,
                deadline = spec.deadline,
            )
        tasks[task.id] = task
        return task.toDTO()
    }

    override fun deleteTask(taskID: TaskID): Boolean = tasks.remove(taskID) != null

    override fun renameTask(
        taskID: TaskID,
        newTitle: String,
    ): Boolean {
        val task = tasks[taskID] ?: return false
        task.setTitle(newTitle)
        return true
    }

    override fun changeDescription(
        taskID: TaskID,
        newDescription: String,
    ): Boolean {
        val task = tasks[taskID] ?: return false
        task.setDescription(newDescription)
        return true
    }

    override fun setDeadline(
        taskID: TaskID,
        deadline: Instant?,
    ): Boolean {
        val task = tasks[taskID] ?: return false
        task.setDeadline(deadline)
        return true
    }

    override fun completeTask(
        taskID: TaskID,
        at: Instant?,
    ): Boolean {
        val task = tasks[taskID] ?: return false
        task.complete(at)
        return true
    }

    override fun uncompleteTask(taskID: TaskID): Boolean {
        val task = tasks[taskID] ?: return false
        task.uncomplete()
        return true
    }

    override fun getTask(taskID: TaskID): TaskDTO? = tasks[taskID]?.toDTO()

    override fun listTasks(): List<TaskDTO> = tasks.values.map(Task::toDTO)
}
