package task.tracker.server.auth

import kotlinx.datetime.Instant

data class Session(
    val token: String,
    val userIDRaw: String,
    val createdAt: Instant,
    val expiresAt: Instant,
)
