package api

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.TaskDTO
import model.TokenStore

private const val BASE_URL = "http://localhost:8080"

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

object TaskApi {
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    // --- AUTH -------------------------------------------------------------

    suspend fun login(username: String): String {
        val bodyJson = json.encodeToString(LoginRequest(username))

        val response =
            window
                .fetch(
                    "$BASE_URL/login",
                    jsObject {
                        method = "POST"
                        headers =
                            jsObject {
                                this["Content-Type"] = "application/json"
                            }
                        body = bodyJson
                    },
                ).await()

        if (!response.ok) {
            val msg = response.text().await()
            throw Exception("Login failed: HTTP ${response.status} ($msg)")
        }

        val text = response.text().await()
        val loginResp = json.decodeFromString<LoginResponse>(text)

        TokenStore.token = loginResp.token
        return loginResp.token
    }

    // --- TASKS ------------------------------------------------------------

    suspend fun listTasks(): List<TaskDTO> {
        val token = TokenStore.token ?: throw Exception("Not logged in")

        val response =
            window
                .fetch(
                    "$BASE_URL/tasks",
                    jsObject {
                        method = "GET"
                        headers =
                            jsObject {
                                this["Authorization"] = "Bearer $token"
                            }
                    },
                ).await()

        if (!response.ok) throw Exception("HTTP ${response.status}")

        val body = response.text().await()
        return json.decodeFromString(body)
    }

    suspend fun createTask(
        title: String,
        description: String = "",
    ): TaskDTO {
        val token = TokenStore.token ?: throw Exception("Not logged in")

        val request = CreateTaskRequest(title = title, description = description)
        val bodyJson = json.encodeToString(request)

        val response =
            window
                .fetch(
                    "$BASE_URL/tasks",
                    jsObject {
                        method = "POST"
                        headers =
                            jsObject {
                                this["Authorization"] = "Bearer $token"
                                this["Content-Type"] = "application/json"
                            }
                        body = bodyJson
                    },
                ).await()

        if (!response.ok) {
            val msg = response.text().await()
            throw Exception("Create failed: HTTP ${response.status} ($msg)")
        }

        val text = response.text().await()
        return json.decodeFromString(text)
    }
}

/** Helper to build plain JS objects in Kotlin/JS **/
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun <T> jsObject(builder: T.() -> Unit): T = (js("({})") as T).apply(builder)
