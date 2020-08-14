package dev.nibbles.android.room

import androidx.sqlite.db.SupportSQLiteProgram
import org.sqlite.database.sqlite.SQLiteProgram

open class Program(private val delegateProgram: SQLiteProgram) : SupportSQLiteProgram {

    override fun bindNull(index: Int) {
        delegateProgram.bindNull(index)
    }

    override fun bindLong(index: Int, value: Long) {
        delegateProgram.bindLong(index, value)
    }

    override fun bindDouble(index: Int, value: Double) {
        delegateProgram.bindDouble(index, value)
    }

    override fun bindString(index: Int, value: String) {
        delegateProgram.bindString(index, value)
    }

    override fun bindBlob(index: Int, value: ByteArray) {
        delegateProgram.bindBlob(index, value)
    }

    override fun clearBindings() {
        delegateProgram.clearBindings()
    }

    override fun close() {
        delegateProgram.close()
    }

}