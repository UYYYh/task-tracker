package task.tracker.app

interface TaskManager {
    fun forUser(userID: UserID): UserTaskManager
}
