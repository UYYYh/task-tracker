package com.example.task.tracker.infra.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

fun createDataSource(): DataSource {
    val config =
        HikariConfig().apply {
            jdbcUrl = System.getenv("TASK_TRACKER_DB_URL")
            username = System.getenv("TASK_TRACKER_DB_USERNAME")
            password = System.getenv("TASK_TRACKER_DB_PASSWORD")
            maximumPoolSize = 10
        }
    return HikariDataSource(config)
}
