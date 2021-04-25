package dk.sdu.ap.apnotepad

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase

class DBManager(private val context: Context) {
    private var dbHelper: DatabaseHelper? = null
    private var database: SQLiteDatabase? = null
    @Throws(SQLException::class)
    fun open(): DBManager {
        dbHelper = DatabaseHelper(context)
        database = dbHelper!!.writableDatabase
        return this
    }

    fun close() {
        dbHelper!!.close()
    }

    fun insertNotes(title: String?, text: String?, type: Int?, folder_id: Int?) {
        val contentValue = ContentValues()
        contentValue.put(DatabaseHelper.TITLE_NOTES, title)
        contentValue.put(DatabaseHelper.TEXT_NOTES, text)
        contentValue.put(DatabaseHelper.TYPE_NOTES, type)
        contentValue.put(DatabaseHelper.FOLDER_ID_NOTES, folder_id)
        database!!.insert(DatabaseHelper.TABLE_NAME_NOTES, null, contentValue)
    }

    fun insertFolders(title: String?, parent_id: Int?) {
        val contentValue = ContentValues()
        contentValue.put(DatabaseHelper.TITLE_NOTES, title)
        contentValue.put(DatabaseHelper.FOLDER_ID_NOTES, parent_id)
        database!!.insert(DatabaseHelper.TABLE_NAME_NOTES, null, contentValue)
    }

    fun fetchNotes(): Cursor? {
        val columns = arrayOf(DatabaseHelper._ID_NOTES, DatabaseHelper.TITLE_NOTES, DatabaseHelper.TEXT_NOTES, DatabaseHelper.TYPE_NOTES, DatabaseHelper.FOLDER_ID_NOTES)
        val cursor =
            database!!.query(DatabaseHelper.TABLE_NAME_NOTES, columns, null, null, null, null, null)
        cursor?.moveToFirst()
        return cursor
    }

    fun fetchFolders(): Cursor? {
        val columns = arrayOf(DatabaseHelper._ID_FOLDERS, DatabaseHelper.TITLE_FOLDERS, DatabaseHelper.PARENT_ID_FOLDERS)
        val cursor =
            database!!.query(DatabaseHelper.TABLE_NAME_FOLDERS, columns, null, null, null, null, null)
        cursor?.moveToFirst()
        return cursor
    }

    fun deleteNotes(_id: Long) {
        database!!.delete(DatabaseHelper.TABLE_NAME_NOTES, DatabaseHelper._ID_NOTES + "=" + _id, null)
    }

    fun deleteFolders(_id: Long) {
        database!!.delete(DatabaseHelper.TABLE_NAME_FOLDERS, DatabaseHelper._ID_FOLDERS + "=" + _id, null)
    }
}