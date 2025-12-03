package task.tracker.app

import java.util.UUID

@JvmInline
value class UserID(
    val raw: String,
) {
    override fun toString(): String = raw

    companion object {
        fun new() = UserID(raw = UUID.randomUUID().toString())
    }
}
