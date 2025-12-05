package model

data class TaskListState(
    val tasks: List<TaskDTO> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
