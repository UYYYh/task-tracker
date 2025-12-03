package task.tracker.server.auth.infra

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import task.tracker.server.auth.Session
import task.tracker.server.auth.SessionRepository
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

class InMemorySessionRepository(
    private val clock: () -> Instant = { Clock.System.now() },
) : SessionRepository {
    private val sessions = ConcurrentHashMap<String, Session>()
    private val secureRandom = SecureRandom()
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    override fun createSession(
        userIDRaw: String,
        ttlSeconds: Long,
    ): Session {
        val now = clock()
        val expiresAt = Instant.fromEpochSeconds(now.epochSeconds + ttlSeconds)
        val token = generateToken()
        val session =
            Session(
                token = token,
                userIDRaw = userIDRaw,
                createdAt = now,
                expiresAt = expiresAt,
            )
        sessions[token] = session
        return session
    }

    override fun findByToken(token: String): Session? {
        val session = sessions[token] ?: return null
        val now = clock()
        return if (session.expiresAt > now) {
            session
        } else {
            // Remove expired sessions lazily
            sessions.remove(token)
            null
        }
    }

    override fun revokeToken(token: String) {
        sessions.remove(token)
    }

    private fun generateToken(bytes: Int = 32): String {
        val buf = ByteArray(bytes)
        secureRandom.nextBytes(buf)
        return encoder.encodeToString(buf)
    }
}
