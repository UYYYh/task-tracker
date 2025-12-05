package presenter

import api.TaskApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import view.TaskListView

class TaskListPresenter(
    private val view: TaskListView,
) {
    private val scope = MainScope()

    fun attach() {
        view.onRefreshClicked = { loadTasks() }
        loadTasks()
    }

    private fun loadTasks() {
        view.showLoading()

        scope.launch {
            try {
                val tasks = TaskApi.listTasks()

                view.showTasks(tasks)
            } catch (e: Exception) {
                view.showError("Failed to load tasks: ${e.message}")
            }
        }
    }
}
