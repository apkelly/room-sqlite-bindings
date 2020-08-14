package dev.nibbles.android.room

import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.os.CancellationSignal
import android.util.Pair
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteStatement
import org.sqlite.database.sqlite.SQLiteCursor
import org.sqlite.database.sqlite.SQLiteDatabase
import java.util.*


internal class Database(private val delegateDatabase: SQLiteDatabase) : SupportSQLiteDatabase {

    companion object {
        private val CONFLICT_VALUES = arrayOf(
            "",
            " OR ROLLBACK ",
            " OR ABORT ",
            " OR FAIL ",
            " OR IGNORE ",
            " OR REPLACE "
        )
    }

    override fun compileStatement(sql: String): SupportSQLiteStatement {
        return Statement(delegateDatabase.compileStatement(sql))
    }

    override fun beginTransaction() {
        delegateDatabase.beginTransaction()
    }

    override fun beginTransactionNonExclusive() {
        delegateDatabase.beginTransactionNonExclusive()
    }

    override fun beginTransactionWithListener(listener: android.database.sqlite.SQLiteTransactionListener) {
        delegateDatabase.beginTransactionWithListener(
            object : org.sqlite.database.sqlite.SQLiteTransactionListener {
                override fun onBegin() {
                    listener.onBegin()
                }

                override fun onCommit() {
                    listener.onCommit()
                }

                override fun onRollback() {
                    listener.onRollback()
                }
            })
    }

    override fun beginTransactionWithListenerNonExclusive(listener: android.database.sqlite.SQLiteTransactionListener) {
        delegateDatabase.beginTransactionWithListenerNonExclusive(
            object : org.sqlite.database.sqlite.SQLiteTransactionListener {
                override fun onBegin() {
                    listener.onBegin()
                }

                override fun onCommit() {
                    listener.onCommit()
                }

                override fun onRollback() {
                    listener.onRollback()
                }
            })
    }

    override fun endTransaction() {
        delegateDatabase.endTransaction()
    }

    override fun setTransactionSuccessful() {
        delegateDatabase.setTransactionSuccessful()
    }

    override fun inTransaction(): Boolean {
        if (delegateDatabase.isOpen) {
            return delegateDatabase.inTransaction()
        }
        throw IllegalStateException("You should not be doing this on a closed database")
    }

    override fun isDbLockedByCurrentThread(): Boolean {
        if (delegateDatabase.isOpen) {
            return delegateDatabase.isDbLockedByCurrentThread
        }
        throw IllegalStateException("You should not be doing this on a closed database")
    }

    override fun yieldIfContendedSafely(): Boolean {
        if (delegateDatabase.isOpen) {
            return delegateDatabase.yieldIfContendedSafely()
        }
        throw IllegalStateException("You should not be doing this on a closed database")
    }

    override fun yieldIfContendedSafely(sleepAfterYieldDelay: Long): Boolean {
        if (delegateDatabase.isOpen) {
            return delegateDatabase.yieldIfContendedSafely(sleepAfterYieldDelay)
        }
        throw IllegalStateException("You should not be doing this on a closed database")
    }

    override fun getVersion(): Int {
        return delegateDatabase.version
    }

    override fun setVersion(version: Int) {
        delegateDatabase.version = version
    }

    override fun getMaximumSize(): Long {
        return delegateDatabase.maximumSize
    }

    override fun setMaximumSize(numBytes: Long): Long {
        return delegateDatabase.setMaximumSize(numBytes)
    }

    override fun getPageSize(): Long {
        return delegateDatabase.pageSize
    }

    override fun setPageSize(numBytes: Long) {
        delegateDatabase.pageSize = numBytes
    }

    override fun query(sql: String): Cursor {
        return query(SimpleSQLiteQuery(sql))
    }

    override fun query(sql: String, selectionArgs: Array<Any>): Cursor {
        return query(SimpleSQLiteQuery(sql, selectionArgs))
    }

    override fun query(supportQuery: SupportSQLiteQuery): Cursor {
        return query(supportQuery, null)
    }

    override fun query(
        supportQuery: SupportSQLiteQuery,
        signal: CancellationSignal?
    ): Cursor {
        val bindings = BindingsRecorder()
        supportQuery.bindTo(bindings)

        return delegateDatabase.rawQueryWithFactory(
            { _, driver, table, query -> SQLiteCursor(driver, table, query) },
            supportQuery.sql,
            bindings.getBindings(),
            null
        )
    }

    override fun insert(
        table: String, conflictAlgorithm: Int,
        values: ContentValues
    ): Long {
        return delegateDatabase.insertWithOnConflict(table, null, values, conflictAlgorithm)
    }

    override fun delete(
        table: String,
        whereClause: String,
        whereArgs: Array<Any>
    ): Int {
        val query = ("DELETE FROM " + table
                + if (whereClause.isEmpty()) "" else " WHERE $whereClause")
        val statement = compileStatement(query)
        return try {
            SimpleSQLiteQuery.bind(statement, whereArgs)
            statement.executeUpdateDelete()
        } finally {
            try {
                statement.close()
            } catch (e: Exception) {
                throw RuntimeException("Exception attempting to close statement", e)
            }
        }
    }

    override fun update(
        table: String, conflictAlgorithm: Int, values: ContentValues,
        whereClause: String, whereArgs: Array<Any>
    ): Int {
        // taken from SQLiteDatabase class.
        require(values.size() != 0) { "Empty values" }
        val sql = StringBuilder(120)
        sql.append("UPDATE ")
        sql.append(CONFLICT_VALUES[conflictAlgorithm])
        sql.append(table)
        sql.append(" SET ")

        // move all bind args to one array
        val setValuesSize = values.size()
        val bindArgsSize = setValuesSize + whereArgs.size
        val bindArgs = arrayOfNulls<Any>(bindArgsSize)
        var i = 0
        for (colName in values.keySet()) {
            sql.append(if (i > 0) "," else "")
            sql.append(colName)
            bindArgs[i++] = values[colName]
            sql.append("=?")
        }
        i = setValuesSize
        while (i < bindArgsSize) {
            bindArgs[i] = whereArgs[i - setValuesSize]
            i++
        }
        if (whereClause.isNotEmpty()) {
            sql.append(" WHERE ")
            sql.append(whereClause)
        }
        val statement = compileStatement(sql.toString())
        return try {
            SimpleSQLiteQuery.bind(statement, bindArgs)
            statement.executeUpdateDelete()
        } finally {
            try {
                statement.close()
            } catch (e: Exception) {
                throw RuntimeException("Exception attempting to close statement", e)
            }
        }
    }

    @Throws(SQLException::class)
    override fun execSQL(sql: String) {
        delegateDatabase.execSQL(sql)
    }

    @Throws(SQLException::class)
    override fun execSQL(
        sql: String,
        bindArgs: Array<Any>
    ) {
        delegateDatabase.execSQL(sql, bindArgs)
    }

    override fun isReadOnly(): Boolean {
        return delegateDatabase.isReadOnly
    }

    override fun isOpen(): Boolean {
        return delegateDatabase.isOpen
    }

    override fun needUpgrade(newVersion: Int): Boolean {
        return delegateDatabase.needUpgrade(newVersion)
    }

    override fun getPath(): String {
        return delegateDatabase.path
    }

    override fun setLocale(locale: Locale?) {
        delegateDatabase.setLocale(locale)
    }

    override fun setMaxSqlCacheSize(cacheSize: Int) {
        delegateDatabase.setMaxSqlCacheSize(cacheSize)
    }

    override fun setForeignKeyConstraintsEnabled(enable: Boolean) {
        delegateDatabase.setForeignKeyConstraintsEnabled(enable)
    }

    override fun enableWriteAheadLogging(): Boolean {
        return delegateDatabase.enableWriteAheadLogging()
    }

    override fun disableWriteAheadLogging() {
        delegateDatabase.disableWriteAheadLogging()
    }

    override fun isWriteAheadLoggingEnabled(): Boolean {
        return delegateDatabase.isWriteAheadLoggingEnabled
    }

    override fun getAttachedDbs(): MutableList<Pair<String, String>>? {
        return delegateDatabase.attachedDbs
    }

    override fun isDatabaseIntegrityOk(): Boolean {
        return delegateDatabase.isDatabaseIntegrityOk
    }

    override fun close() {
        delegateDatabase.close()
    }
}