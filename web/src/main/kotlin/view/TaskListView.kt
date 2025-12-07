package view

import model.TaskDTO

interface TaskListView {
    // Presenter → View (output)
    fun showStatus(message: String)

    fun setLoggedInUser(username: String?)

    fun showTasks(tasks: List<TaskDTO>)

    fun clearTaskInputs()

    fun showTaskDetails(task: TaskDTO)

    fun hideTaskDetails()

    // View → Presenter (events, set by presenter)
    var onLoginClicked: ((username: String) -> Unit)?
    var onCreateTaskClicked: ((title: String, description: String, deadlineRaw: String) -> Unit)?
    var onDeleteTaskClicked: ((taskId: String) -> Unit)?
    var onTaskClicked: ((taskId: String) -> Unit)?
    var onTaskEditConfirmed: ((taskId: String, title: String, description: String, deadlineRaw: String) -> Unit)?
}
