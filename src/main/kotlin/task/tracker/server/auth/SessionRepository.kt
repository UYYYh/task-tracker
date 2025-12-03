package task.tracker.server.auth

interface SessionRepository {
    fun createSession(
        userIDRaw: String,
        ttlSeconds: Long,
    ): Session

    fun findByToken(token: String): Session?

    fun revokeToken(token: String)
}
