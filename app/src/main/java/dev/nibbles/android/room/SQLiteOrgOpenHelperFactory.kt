package dev.nibbles.android.room

import androidx.sqlite.db.SupportSQLiteOpenHelper

object SQLiteOrgOpenHelperFactory : SupportSQLiteOpenHelper.Factory {
    init {
        System.loadLibrary("sqliteX")
    }

    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        return Helper(
            configuration.context,
            configuration.name,
            configuration.callback
        )
    }
}