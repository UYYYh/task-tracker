package task.tracker.server.auth

import task.tracker.app.UserID

data class AuthenticatedUser(
    val userIDRaw: String,
) {
    fun toUserID(): UserID = UserID(userIDRaw)
}
