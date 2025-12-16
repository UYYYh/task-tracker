package com.example.task.tracker.infra.db

import task.tracker.app.TaskManager
import task.tracker.app.UserID
import task.tracker.app.UserTaskManager

class DbTaskManager : TaskManager {
    override fun forUser(userID: UserID): UserTaskManager {
        TODO("Not yet implemented")
    }
}
