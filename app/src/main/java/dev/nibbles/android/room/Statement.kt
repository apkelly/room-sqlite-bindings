package dev.nibbles.android.room

import androidx.sqlite.db.SupportSQLiteStatement
import org.sqlite.database.sqlite.SQLiteStatement

class Statement(private val delegateStatement: SQLiteStatement) : Program(delegateStatement),
    SupportSQLiteStatement {

    override fun execute() {
        delegateStatement.execute()
    }

    override fun executeUpdateDelete(): Int {
        return delegateStatement.executeUpdateDelete()
    }

    override fun executeInsert(): Long {
        return delegateStatement.executeInsert()
    }

    override fun simpleQueryForLong(): Long {
        return delegateStatement.simpleQueryForLong()
    }

    override fun simpleQueryForString(): String {
        return delegateStatement.simpleQueryForString()
    }
}