package model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TaskDTO(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val deadline: Instant?,
)
