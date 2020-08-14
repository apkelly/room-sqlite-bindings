package dev.nibbles.android.room

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.sqlite.database.sqlite.SQLiteDatabase
import org.sqlite.database.sqlite.SQLiteOpenHelper


class Helper(
    context: Context,
    name: String?,
    callback: SupportSQLiteOpenHelper.Callback
) : SupportSQLiteOpenHelper {
    private var delegateHelper: OpenHelper

    init {
        // If we're passed a name, then create the correct file, otherwise create an in-memory db.
        val filename = name?.let {
            val dbPath = context.getDatabasePath(it)
            if (dbPath?.parentFile?.exists() == false) {
                dbPath.parentFile?.mkdirs()
            }

            dbPath.path
        }

        delegateHelper = OpenHelper(context, filename, callback)
    }

    @Synchronized
    override fun getDatabaseName(): String {
        return delegateHelper.databaseName
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Synchronized
    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        delegateHelper.setWriteAheadLoggingEnabled(enabled)
    }

    @Synchronized
    override fun getWritableDatabase(): SupportSQLiteDatabase {
        return delegateHelper.getWritableSupportDatabase()
    }

    override fun getReadableDatabase(): SupportSQLiteDatabase {
        return writableDatabase
    }

    @Synchronized
    override fun close() {
        delegateHelper.close()
    }

    internal class OpenHelper(
        context: Context?,
        name: String?,
        private val callback: SupportSQLiteOpenHelper.Callback
    ) :
        SQLiteOpenHelper(context, name, null, callback.version) {
        private var migrated = false
        private var wrappedDatabase: Database? = null

        fun getWritableSupportDatabase(): SupportSQLiteDatabase {
            migrated = false
            val db = super.getWritableDatabase()
            if (migrated) {
                close()
                return getWritableSupportDatabase()
            }
            return getWrappedDb(db)
        }

        @Synchronized
        private fun getWrappedDb(db: SQLiteDatabase): SupportSQLiteDatabase {
            return wrappedDatabase ?: Database(db).also {
                wrappedDatabase = it
            }
        }

        override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
            callback.onCreate(getWrappedDb(sqLiteDatabase))
        }

        override fun onUpgrade(
            sqLiteDatabase: SQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
        ) {
            migrated = true
            callback.onUpgrade(getWrappedDb(sqLiteDatabase), oldVersion, newVersion)
        }

        override fun onConfigure(db: SQLiteDatabase) {
            callback.onConfigure(getWrappedDb(db))
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            migrated = true
            callback.onDowngrade(getWrappedDb(db), oldVersion, newVersion)
        }

        override fun onOpen(db: SQLiteDatabase) {
            if (!migrated) {
                // from Google: "if we've migrated, we'll re-open the db so we  should not call the callback."
                callback.onOpen(getWrappedDb(db))
            }
        }

        @Synchronized
        override fun close() {
            super.close()
            wrappedDatabase = null
        }
    }

}