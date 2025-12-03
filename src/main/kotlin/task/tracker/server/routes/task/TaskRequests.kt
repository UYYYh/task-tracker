import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String = "",
    val deadline: Instant? = null,
)

@Serializable
data class RenameRequest(
    val newTitle: String,
)

@Serializable
data class ChangeDescriptionRequest(
    val newDescription: String,
)

@Serializable
data class ChangeDeadlineRequest(
    val deadline: Instant?, // null to clear deadline
)
