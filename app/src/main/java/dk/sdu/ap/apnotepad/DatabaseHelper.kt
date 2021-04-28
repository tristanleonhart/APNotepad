package dk.sdu.ap.apnotepad

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper private constructor(c: Context?) : SQLiteOpenHelper(c, DB_NAME, null, DB_VERSION) {

    fun insertNote(note: Note, folder_id: Long) : Long {
        val contentValues = ContentValues()
        contentValues.put(EMOJI_NOTES, note.emoji)
        contentValues.put(TITLE_NOTES, note.title)
        contentValues.put(TEXT_NOTES, note.text)
        contentValues.put(TYPE_NOTES, note.type)
        contentValues.put(FOLDER_ID_NOTES, folder_id)
        return writableDatabase.insert(TABLE_NAME_NOTES, null, contentValues)
    }

    fun updateNote(note: Note) {
        val contentValues = ContentValues()
        contentValues.put(EMOJI_NOTES, note.emoji)
        contentValues.put(TITLE_NOTES, note.title)
        contentValues.put(TEXT_NOTES, note.text)
        writableDatabase.update(TABLE_NAME_NOTES, contentValues, _ID_NOTES + "=" + note.id, null)
    }

    fun insertFolder(emoji: String?, name: String?, parent_id: Long) : Long {
        val contentValues = ContentValues()
        contentValues.put(EMOJI_FOLDERS, emoji)
        contentValues.put(NAME_FOLDERS, name)
        contentValues.put(PARENT_ID_FOLDERS, parent_id)
        return writableDatabase.insert(TABLE_NAME_FOLDERS, null, contentValues)
    }

    fun updateFolder(_id: Long, emoji: String?, name: String?) {
        val contentValues = ContentValues()
        contentValues.put(EMOJI_FOLDERS, emoji)
        contentValues.put(NAME_FOLDERS, name)
        writableDatabase.update(TABLE_NAME_FOLDERS, contentValues, "$_ID_FOLDERS=$_id", null)
    }

    fun getNote(note_id: Long) : Note {
        val columns = arrayOf(EMOJI_NOTES, TITLE_NOTES, TEXT_NOTES, TYPE_NOTES)
        val selection = "$_ID_NOTES = ?"
        val selectionArgs = arrayOf("" + note_id)
        val cursor = writableDatabase.query(
            TABLE_NAME_NOTES,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        var note: Note? = null
        if (cursor.moveToNext()) {
            val index_type = cursor.getColumnIndex(TYPE_NOTES)
            val index_emoji = cursor.getColumnIndex(EMOJI_NOTES)
            val index_title = cursor.getColumnIndex(TITLE_NOTES)
            val index_text = cursor.getColumnIndex(TEXT_NOTES)
            val type = cursor.getInt(index_type)
            val emoji = cursor.getString(index_emoji)
            val title = cursor.getString(index_title)
            val text = cursor.getString(index_text)
            note = Note(note_id, type, emoji, title, text)
        }
        cursor.close()
        return note as Note
    }

    fun getFolderItems(folder_id: Long) : ArrayList<FolderItem> {
        val folderItems: ArrayList<FolderItem> = ArrayList()
        // folders
        val cursorFolders = fetchFoldersInFolder(folder_id)
        if (cursorFolders != null) {
            val index_id = cursorFolders.getColumnIndex(_ID_FOLDERS)
            val index_emoji = cursorFolders.getColumnIndex(EMOJI_FOLDERS)
            val index_name = cursorFolders.getColumnIndex(NAME_FOLDERS)
            while (cursorFolders.moveToNext())
            {
                val _id = cursorFolders.getLong(index_id)
                val emoji = cursorFolders.getString(index_emoji)
                val name = cursorFolders.getString(index_name)
                folderItems.add(FolderItem(false, _id, emoji, name, null))
            }
            cursorFolders.close()
        }
        // notes
        val cursorNotes = fetchNotesInFolder(folder_id)
        if (cursorNotes != null) {
            val index_id = cursorNotes.getColumnIndex(_ID_NOTES)
            val index_type = cursorNotes.getColumnIndex(TYPE_NOTES)
            val index_emoji = cursorNotes.getColumnIndex(EMOJI_NOTES)
            val index_title = cursorNotes.getColumnIndex(TITLE_NOTES)
            val index_text = cursorNotes.getColumnIndex(TEXT_NOTES)
            while (cursorNotes.moveToNext())
            {
                val _id = cursorNotes.getLong(index_id)
                val type = cursorNotes.getInt(index_type)
                val emoji = cursorNotes.getString(index_emoji)
                val title = cursorNotes.getString(index_title)
                val text = cursorNotes.getString(index_text)
                val preview = NoteUtils.getNotePreview(type, text)
                folderItems.add(FolderItem(true, _id, emoji, title, preview))
            }
            cursorNotes.close()
        }
        return folderItems
    }

    private fun fetchNotesInFolder(folder_id: Long?): Cursor? {
        val columns = arrayOf(_ID_NOTES, EMOJI_NOTES, TITLE_NOTES, TEXT_NOTES, TYPE_NOTES)
        val selection = "$FOLDER_ID_NOTES = ?"
        val selectionArgs = arrayOf("" + folder_id)
        val sortOrder = "$_ID_NOTES ASC"
        return writableDatabase.query(
            TABLE_NAME_NOTES,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
    }

    private fun fetchFoldersInFolder(folder_id: Long?): Cursor? {
        val columns = arrayOf(_ID_FOLDERS, EMOJI_FOLDERS, NAME_FOLDERS)
        val selection = "$PARENT_ID_FOLDERS = ?"
        val selectionArgs = arrayOf("" + folder_id)
        val sortOrder = "$_ID_FOLDERS ASC"
        return writableDatabase.query(
            TABLE_NAME_FOLDERS,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
    }

    fun deleteNote(note_id: Long) {
        writableDatabase.delete(TABLE_NAME_NOTES, "$_ID_NOTES=$note_id", null)
    }

    fun deleteFolder(_id: Long) {
        writableDatabase.delete(TABLE_NAME_FOLDERS, "$_ID_FOLDERS=$_id", null)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_NOTES)
        db.execSQL(CREATE_TABLE_FOLDERS)
        insertRootFolder(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_NOTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_FOLDERS")
        onCreate(db)
    }

    private fun insertRootFolder(db: SQLiteDatabase) {
        val contentValues = ContentValues()
        contentValues.put(_ID_FOLDERS, 0)
        contentValues.put(NAME_FOLDERS, "ROOT")
        db.insert(TABLE_NAME_FOLDERS, null, contentValues)
    }

    companion object {
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context) : DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(context.applicationContext)
            }
            return instance as DatabaseHelper
        }

        const val TABLE_NAME_NOTES = "notes"
        const val _ID_NOTES = "_id"
        const val EMOJI_NOTES = "emoji"
        const val TITLE_NOTES = "title"
        const val TEXT_NOTES = "text"
        const val TYPE_NOTES = "type"
        const val FOLDER_ID_NOTES = "folder_id"

        const val TABLE_NAME_FOLDERS = "folders"
        const val _ID_FOLDERS = "_id"
        const val EMOJI_FOLDERS = "emoji"
        const val NAME_FOLDERS = "name"
        const val PARENT_ID_FOLDERS = "parent_id"

        const val DB_NAME = "AP_NOTEPAD.DB"
        const val DB_VERSION = 1

        private const val CREATE_TABLE_NOTES =
            "create table " + TABLE_NAME_NOTES + "(" +
                    _ID_NOTES + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    EMOJI_NOTES + " TEXT, " +
                    TITLE_NOTES + " TEXT, " +
                    TEXT_NOTES + " TEXT, " +
                    TYPE_NOTES + " INTEGER NOT NULL, " +
                    FOLDER_ID_NOTES + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + FOLDER_ID_NOTES + ") REFERENCES " + TABLE_NAME_FOLDERS + "(" + _ID_FOLDERS + ") ON DELETE CASCADE" +
                    ");"
        private const val CREATE_TABLE_FOLDERS =
            "create table " + TABLE_NAME_FOLDERS + "(" +
                    _ID_FOLDERS + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    EMOJI_FOLDERS + " TEXT, " +
                    NAME_FOLDERS + " TEXT NOT NULL, " +
                    PARENT_ID_FOLDERS + " INTEGER, " +
                    "FOREIGN KEY (" + PARENT_ID_FOLDERS + ") REFERENCES " + TABLE_NAME_FOLDERS + "(" + _ID_FOLDERS + ") ON DELETE CASCADE" +
                    ");"
    }
}