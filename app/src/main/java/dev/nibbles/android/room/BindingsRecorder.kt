package dev.nibbles.android.room

import android.util.SparseArray
import androidx.sqlite.db.SupportSQLiteProgram


internal class BindingsRecorder : SupportSQLiteProgram {
    private val bindings = SparseArray<Any?>()

    override fun bindNull(index: Int) {
        bindings.put(index, null)
    }

    override fun bindLong(index: Int, value: Long) {
        bindings.put(index, value)
    }

    override fun bindDouble(index: Int, value: Double) {
        bindings.put(index, value)
    }

    override fun bindString(index: Int, value: String) {
        bindings.put(index, value)
    }

    override fun bindBlob(index: Int, value: ByteArray) {
        bindings.put(index, value)
    }

    override fun clearBindings() {
        bindings.clear()
    }

    override fun close() {
        clearBindings()
    }

    fun getBindings(): Array<String?> {
        val result = arrayOfNulls<String>(bindings.size())
        for (i in 0 until bindings.size()) {
            val key = bindings.keyAt(i)
            val binding = bindings[key]
            if (binding != null) {
                result[i] = bindings[key].toString()
            } else {
                result[i] = ""
            }
        }
        return result
    }
}