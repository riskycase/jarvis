package com.riskycase.jarvis

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DATABASE_NAME: String = "jarvis"
const val DATABASE_VERSION: Int = 1

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val TABLE_NAME: String = "snaps"
    private val KEY_ID: String = "id"
    private val KEY_SENDER: String = "sender"
    private val KEY_TIME: String = "time"

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(
            "CREATE TABLE $TABLE_NAME($KEY_ID TEXT PRIMARY KEY, $KEY_SENDER TEXT, $KEY_TIME INTEGER)"
        )
    }

    fun add(snap: Snap){
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(KEY_ID, snap.getKey())
        values.put(KEY_SENDER, snap.getSender())
        values.put(KEY_TIME, snap.getTime())

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAll(): List<Snap> {
        val snapList = ArrayList<Snap>()
        val db = this.writableDatabase

        val cursor = db.query(TABLE_NAME,
            arrayOf(KEY_ID, KEY_SENDER, KEY_TIME),
            null,
            null,
            null,
            null,
            KEY_TIME.plus(" DESC")
        )

        if(cursor.moveToFirst()){
            do {
                snapList.add(
                    Snap(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getLong(2)
                    )
                )
            } while (cursor.moveToNext())
        }

        db.close()
        cursor.close()

        return snapList
    }

    fun removeAll() {
        val db = this.writableDatabase

        db.delete(TABLE_NAME, null, null)

        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

}