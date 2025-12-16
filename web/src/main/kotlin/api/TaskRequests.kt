package api

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
)

@Serializable
data class LoginResponse(
    val token: String,
)

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String = "",
    val deadline: Instant? = null,
)

@Serializable
data class UpdateTaskRequest(
    val deadline: Instant?,
    val description: String,
    val title: String,
    val completionTime: Instant?,
)
