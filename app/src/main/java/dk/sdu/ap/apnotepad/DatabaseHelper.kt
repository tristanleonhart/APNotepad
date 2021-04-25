package dk.sdu.ap.apnotepad

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(c: Context?) : SQLiteOpenHelper(c, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_NOTES)
        db.execSQL(CREATE_TABLE_FOLDERS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_NOTES)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FOLDERS)
        onCreate(db)
    }

    companion object {
        const val TABLE_NAME_NOTES = "notes"
        const val _ID_NOTES = "_id"
        const val TITLE_NOTES = "title"
        const val TEXT_NOTES = "text"
        const val TYPE_NOTES = "type"
        const val FOLDER_ID_NOTES = "folder_id"

        const val TABLE_NAME_FOLDERS = "folders"
        const val _ID_FOLDERS = "_id"
        const val TITLE_FOLDERS = "title"
        const val PARENT_ID_FOLDERS = "parent_id"

        const val DB_NAME = "AP_NOTEPAD.DB"
        const val DB_VERSION = 1
        private const val CREATE_TABLE_NOTES =
            "create table " + TABLE_NAME_NOTES + "(" + _ID_NOTES + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TITLE_NOTES + " TEXT NOT NULL, " + TEXT_NOTES + " TEXT, " + TYPE_NOTES + " INTEGER NOT NULL, " + FOLDER_ID_NOTES + " INTEGER);"
        private const val CREATE_TABLE_FOLDERS =
            "create table " + TABLE_NAME_FOLDERS + "(" + _ID_NOTES + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TITLE_FOLDERS + " TEXT NOT NULL, " + PARENT_ID_FOLDERS + " INTEGER);"


    }
}