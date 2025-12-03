package task.tracker.server.auth

import task.tracker.app.UserID

data class AuthenticatedUser(
    val userIDRaw: String,
) {
    fun toUserId(): UserID = UserID(userIDRaw)
}
