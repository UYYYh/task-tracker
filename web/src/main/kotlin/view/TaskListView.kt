package view

import model.TaskDTO

interface TaskListView {
    fun showLoading()

    fun showTasks(tasks: List<TaskDTO>)

    fun showError(message: String)

    var onRefreshClicked: (() -> Unit)?
}
