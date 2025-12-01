package com.example.task.tracker.dto

import com.example.task.tracker.domain.Task

fun Task.toDTO(): TaskDTO =
    TaskDTO(
        id = id.toString(),
        title = title,
        description = description,
        creationTime = creationTime,
        deadline = deadline,
        completionTime = completionTime,
    )
