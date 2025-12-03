package task.tracker.server

import com.example.configureHTTP
import com.example.configureSerialization
import configureRouting
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import task.tracker.app.TaskManager
import task.tracker.infra.memory.InMemoryTaskManager
import task.tracker.server.auth.SessionRepository
import task.tracker.server.auth.configureTokenAuth
import task.tracker.server.infra.InMemorySessionRepository

fun main(args: Array<String>) {
    EngineMain
        .main(args)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()

    val taskManager: TaskManager = InMemoryTaskManager()
    val sessionRepository: SessionRepository = InMemorySessionRepository()

    configureTokenAuth(sessionRepository)
    configureRouting(taskManager, sessionRepository)
}
