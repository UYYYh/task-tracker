import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.w3c.fetch.CORS
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode

private val URL = "http://localhost:8080"

private val json =
    Json {
        ignoreUnknownKeys = true
    }

suspend fun login(username: String): String {
    val body = json.encodeToString(LoginRequest(username))

    val response =
        window
            .fetch(
                input = URL,
                init =
                    RequestInit(
                        method = "POST",
                        headers =
                            Headers().also {
                                it.append("Content-Type", "application/json")
                            },
                        body = body,
                        mode = RequestMode.CORS,
                    ),
            ).await()

    if (!response.ok) {
        throw RuntimeException("Login failed: ${response.status}")
    }

    val text = response.text().await()
    val payload = json.decodeFromString<LoginResponse>(text)
    return payload.token
}

suspend fun fetchTasks(token: String): List<TaskDTO> {
    val response =
        window
            .fetch(
                input = URL,
                init =
                    RequestInit(
                        method = "GET",
                        headers =
                            Headers().also {
                                it.append("Authorization", "Bearer $token")
                            },
                        mode = RequestMode.CORS,
                    ),
            ).await()

    if (!response.ok) {
        throw RuntimeException("Fetching tasks failed: ${response.status}")
    }

    val text = response.text().await()
    return json.decodeFromString(text)
}
