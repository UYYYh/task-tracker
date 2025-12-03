package com.example.task.tracker.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TaskDTO(
    val id: String,
    val title: String,
    val description: String,
    val creationTime: Instant,
    val deadline: Instant?,
    val completionTime: Instant?,
)
