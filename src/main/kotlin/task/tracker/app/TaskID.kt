package task.tracker.app

import java.util.UUID

@JvmInline
value class TaskID(
    val raw: String,
) {
    override fun toString(): String = raw

    companion object {
        fun new() = TaskID(raw = UUID.randomUUID().toString())
    }
}
