import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TaskDTO(
    val id: String,
    val title: String,
    val description: String,
    val creationTime: Instant,
    val deadline: Instant?,
    val completionTime: Instant?,
)

@Serializable
data class LoginRequest(
    val username: String,
)

@Serializable
data class LoginResponse(
    val token: String,
)
