package com.example.task.tracker.dto

import com.example.task.tracker.domain.Task

fun Task.toDTO(): TaskDTO =
    TaskDTO(
        id = id.toString(),
        title = getTitle(),
        description = getDescription(),
        creationTime = getCreationTime(),
        deadline = getDeadline(),
        completionTime = getCompletionTime(),
    )
