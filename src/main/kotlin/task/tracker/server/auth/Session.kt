package task.tracker.server.auth

import kotlinx.datetime.Instant

data class Session(
    val token: String,
    val userIDRaw: String, // Using raw string instead of UserID to decouple server from app module
    val createdAt: Instant,
    val expiresAt: Instant,
)
