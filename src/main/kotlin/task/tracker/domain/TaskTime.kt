package com.example.task.tracker.domain

import kotlinx.datetime.Instant

internal sealed class TaskTime {
    data class Actual(
        val instant: Instant,
    ) : TaskTime(),
        Comparable<Actual> {
        override fun compareTo(other: Actual): Int = instant.compareTo(other.instant)

        override fun toString(): String = instant.toString()
    }

    object NoDeadLine : TaskTime() {
        override fun toString(): String = "No Deadline"
    }

    object NotYetSet : TaskTime() {
        override fun toString(): String = "Not yet set"
    }
}
