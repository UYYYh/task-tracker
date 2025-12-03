package task.tracker.infra.memory

import task.tracker.app.TaskManager
import task.tracker.app.UserID
import task.tracker.app.UserTaskManager

class InMemoryTaskManager : TaskManager {
    private var users: MutableMap<UserID, UserTaskManager> = mutableMapOf()

    fun newUser(): UserTaskManager {
        val userID = UserID.new()
        return forUser(userID)
    }

    override fun forUser(userID: UserID): UserTaskManager {
        if (!users.containsKey(userID)) {
            users[userID] = InMemoryUserTaskManager()
        }
        return users[userID]!!
    }
}
