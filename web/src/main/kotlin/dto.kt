import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
)

@Serializable
data class LoginResponse(
    val token: String,
)
