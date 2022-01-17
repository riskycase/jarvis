package com.riskycase.jarvis

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val DATABASE_NAME: String = "jarvis"
const val DATABASE_VERSION: Int = 2

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val TABLE_NAME_SNAPS: String = "snaps"
    private val KEY_ID: String = "id"
    private val KEY_SENDER: String = "sender"
    private val KEY_TIME: String = "time"

    private val TABLE_NAME_FILTERS: String = "filters"
    private val KEY_TITLE: String = "title"
    private val KEY_TEXT: String = "text"

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            db.execSQL(
                "CREATE TABLE $TABLE_NAME_SNAPS($KEY_ID TEXT PRIMARY KEY, $KEY_SENDER TEXT, $KEY_TIME INTEGER)"
            )
            db.execSQL(
                "CREATE TABLE $TABLE_NAME_FILTERS($KEY_ID NUMBER PRIMARY KEY, $KEY_TITLE TEXT, $KEY_TEXT TEXT)"
            )
            resetFilters()
        }
    }

    fun addSnap(snap: Snap) {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(KEY_ID, snap.getKey())
        values.put(KEY_SENDER, snap.getSender())
        values.put(KEY_TIME, snap.getTime())

        db.insert(TABLE_NAME_SNAPS, null, values)
        db.close()
    }

    fun getAllSnaps(): List<Snap> {
        val snapList = ArrayList<Snap>()
        val db = this.writableDatabase

        val cursor = db.query(
            TABLE_NAME_SNAPS,
            arrayOf(KEY_ID, KEY_SENDER, KEY_TIME),
            null,
            null,
            null,
            null,
            KEY_TIME.plus(" DESC")
        )

        if (cursor.moveToFirst()) {
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

    fun removeAllSnaps() {
        val db = this.writableDatabase

        db.delete(TABLE_NAME_SNAPS, null, null)

        db.close()
    }

    fun setFilters(filters: Array<Filter>) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME_FILTERS, null, null)
        filters.forEachIndexed { index, filter ->
            val values = ContentValues()
            values.put(KEY_ID, index)
            values.put(KEY_TITLE, filter.title)
            values.put(KEY_TEXT, filter.text)
            db.insert(TABLE_NAME_FILTERS, null, values)
        }
        db.close()
    }

    fun getFilters(): Array<Filter> {
        var filters = emptyArray<Filter>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME_FILTERS,
            arrayOf(KEY_ID, KEY_TITLE, KEY_TITLE),
            null,
            null,
            null,
            null,
            KEY_ID
        )

        if(cursor.moveToFirst()){
            do {
                filters = filters.plusElement(Filter(cursor.getString(1), cursor.getString(2)))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return filters
    }

    fun resetFilters() {
        val db = this.writableDatabase
        var filters = emptyArray<Filter>()
        filters = filters.plusElement(Filter(title = "from $1", text = ""))
        filters = filters.plusElement(Filter(title = "", text = "from $1"))
        filters = filters.plusElement(Filter(title = "$1", text = "sent a Snap"))
        db.delete(TABLE_NAME_FILTERS, null, null)
        filters.forEachIndexed { index, filter ->
            val values = ContentValues()
            values.put(KEY_ID, index)
            values.put(KEY_TITLE, filter.title)
            values.put(KEY_TEXT, filter.text)
            db.insert(TABLE_NAME_FILTERS, null, values)
        }
        db.close()

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_SNAPS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_FILTERS")
        }
        onCreate(db)
    }

}