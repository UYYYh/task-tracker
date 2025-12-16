package com.example.task.tracker.infra.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

fun createDataSource(): DataSource {
    val config =
        HikariConfig().apply {
            jdbcUrl = System.getenv("DB_URL")
            username = System.getenv("DB_USER")
            password = System.getenv("DB_PASSWORD")
            maximumPoolSize = 10
        }
    return HikariDataSource(config)
}
