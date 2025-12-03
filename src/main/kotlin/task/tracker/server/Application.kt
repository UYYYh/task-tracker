package task.tracker.server

import com.example.configureHTTP
import com.example.configureSerialization
import configureRouting
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import task.tracker.app.TaskManager
import task.tracker.infra.memory.InMemoryTaskManager
import task.tracker.server.auth.SessionRepository
import task.tracker.server.auth.configureTokenAuth
import task.tracker.server.auth.infra.InMemorySessionRepository
import task.tracker.server.plugins.configureStatusPages

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
    configureStatusPages()
    configureRouting(taskManager, sessionRepository)
}
