import api.TaskApi
import presenter.TaskListPresenter
import view.TaskListDomView

fun main() {
    val view = TaskListDomView.fromDocument()
    val presenter = TaskListPresenter(TaskApi, view)
    presenter.start()
}
