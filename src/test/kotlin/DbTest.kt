package com.example

import com.example.task.tracker.infra.db.createDataSource
import com.example.task.tracker.infra.db.inTx
import com.example.task.tracker.infra.db.prep
import com.example.task.tracker.infra.db.withConnection
import kotlin.test.Test
import kotlin.test.assertEquals

class DbTest {
    @Test
    fun connectionTest() {
        val ds = createDataSource()

        ds.connection.use { conn ->
            conn.prepareStatement("SELECT 1").use { ps ->
                ps.executeQuery().use { rs ->
                    rs.next()
                    assertEquals(1, rs.getInt(1))
                }
            }
        }
    }
}
