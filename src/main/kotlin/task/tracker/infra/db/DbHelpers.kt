package com.example.task.tracker.infra.db
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource

inline fun <T> DataSource.withConnection(block: (Connection) -> T): T = this.connection.use(block)

inline fun <T> Connection.inTx(block: (Connection) -> T): T {
    val oldAutoCommit = autoCommit
    autoCommit = false
    try {
        val result = block(this)
        commit()
        return result
    } catch (t: Throwable) {
        rollback()
        throw t
    } finally {
        autoCommit = oldAutoCommit
    }
}

inline fun <T> Connection.prep(
    sql: String,
    bind: PreparedStatement.() -> Unit = {},
    read: PreparedStatement.() -> T,
): T =
    prepareStatement(sql).use { ps ->
        ps.bind()
        ps.read()
    }
