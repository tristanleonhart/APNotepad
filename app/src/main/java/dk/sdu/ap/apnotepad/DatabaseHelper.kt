package dk.sdu.ap.apnotepad

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper private constructor(c: Context?) : SQLiteOpenHelper(c, DB_NAME, null, DB_VERSION) {

    fun insertNote(note: Note, folder_id: Long) : Long {
        val contentValues = ContentValues()
        if (note.id != APNotepadConstants.AUTO_INCREMENT_ID) {
            contentValues.put(ID_NOTES, note.id)
        }
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
        writableDatabase.update(TABLE_NAME_NOTES, contentValues, ID_NOTES + "=" + note.id, null)
    }

    fun insertFolder(folder: Folder) : Long {
        val contentValues = ContentValues()
        if (folder.id != APNotepadConstants.AUTO_INCREMENT_ID) {
            contentValues.put(ID_FOLDERS, folder.id)
        }
        contentValues.put(EMOJI_FOLDERS, folder.emoji)
        contentValues.put(NAME_FOLDERS, folder.name)
        contentValues.put(PARENT_ID_FOLDERS, folder.parent_id)
        return writableDatabase.insert(TABLE_NAME_FOLDERS, null, contentValues)
    }

    fun updateFolder(folder: Folder) {
        val contentValues = ContentValues()
        contentValues.put(EMOJI_FOLDERS, folder.emoji)
        contentValues.put(NAME_FOLDERS, folder.name)
        writableDatabase.update(TABLE_NAME_FOLDERS, contentValues, ID_FOLDERS + "=" + folder.id, null)
    }

    fun getNote(id: Long) : Note {
        val columns = arrayOf(EMOJI_NOTES, TITLE_NOTES, TEXT_NOTES, TYPE_NOTES)
        val selection = "$ID_NOTES = ?"
        val selectionArgs = arrayOf("" + id)
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
            val indexEmoji = cursor.getColumnIndex(EMOJI_NOTES)
            val indexTitle = cursor.getColumnIndex(TITLE_NOTES)
            val indexText = cursor.getColumnIndex(TEXT_NOTES)
            val indexType = cursor.getColumnIndex(TYPE_NOTES)
            val emoji = cursor.getString(indexEmoji)
            val title = cursor.getString(indexTitle)
            val text = cursor.getString(indexText)
            val type = cursor.getInt(indexType)
            note = Note(id, type, emoji, title, text)
        }
        cursor.close()
        return note as Note
    }

    fun getFolder(id: Long) : Folder {
        val columns = arrayOf(EMOJI_FOLDERS, NAME_FOLDERS, PARENT_ID_FOLDERS)
        val selection = "$ID_FOLDERS = ?"
        val selectionArgs = arrayOf("" + id)
        val cursor = writableDatabase.query(
            TABLE_NAME_FOLDERS,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        var folder: Folder? = null
        if (cursor.moveToNext()) {
            val indexEmoji = cursor.getColumnIndex(EMOJI_FOLDERS)
            val indexName = cursor.getColumnIndex(NAME_FOLDERS)
            val indexParentId = cursor.getColumnIndex(PARENT_ID_FOLDERS)
            val emoji = cursor.getString(indexEmoji)
            val name = cursor.getString(indexName)
            val parentId = cursor.getLong(indexParentId)
            folder = Folder(id, emoji, name, parentId)
        }
        cursor.close()
        return folder as Folder
    }

    fun getFolderItems(folder_id: Long) : ArrayList<FolderItem> {
        val folderItems: ArrayList<FolderItem> = ArrayList()
        // get all folders in folder
        folderItems.addAll(getFoldersInFolder(folder_id))
        // get all notes in folder
        folderItems.addAll(getNotesInFolder(folder_id))
        return folderItems
    }

    private fun getNotesInFolder(folder_id: Long) : ArrayList<FolderItem> {
        val columns = arrayOf(ID_NOTES, EMOJI_NOTES, TITLE_NOTES, TEXT_NOTES, TYPE_NOTES)
        val selection = "$FOLDER_ID_NOTES = ?"
        val selectionArgs = arrayOf("" + folder_id)
        val sortOrder = "$ID_NOTES DESC"
        val cursor = writableDatabase.query(
            TABLE_NAME_NOTES,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
        val indexId = cursor.getColumnIndex(ID_NOTES)
        val indexEmoji = cursor.getColumnIndex(EMOJI_NOTES)
        val indexTitle = cursor.getColumnIndex(TITLE_NOTES)
        val indexText = cursor.getColumnIndex(TEXT_NOTES)
        val indexType = cursor.getColumnIndex(TYPE_NOTES)
        val folderItems: ArrayList<FolderItem> = ArrayList()
        while (cursor.moveToNext())
        {
            val id = cursor.getLong(indexId)
            val emoji = cursor.getString(indexEmoji)
            val title = cursor.getString(indexTitle)
            val text = cursor.getString(indexText)
            val type = cursor.getInt(indexType)
            val preview = NoteUtils.getNotePreview(type, text)
            folderItems.add(FolderItem(true, id, emoji, title, preview))
        }
        cursor.close()
        return folderItems
    }

    private fun getFoldersInFolder(folder_id: Long) : ArrayList<FolderItem> {
        val columns = arrayOf(ID_FOLDERS, EMOJI_FOLDERS, NAME_FOLDERS)
        val selection = "$PARENT_ID_FOLDERS = ?"
        val selectionArgs = arrayOf("" + folder_id)
        val sortOrder = "$ID_FOLDERS DESC"
        val cursor = writableDatabase.query(
            TABLE_NAME_FOLDERS,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
        val indexId = cursor.getColumnIndex(ID_FOLDERS)
        val indexEmoji = cursor.getColumnIndex(EMOJI_FOLDERS)
        val indexName = cursor.getColumnIndex(NAME_FOLDERS)
        val folderItems: ArrayList<FolderItem> = ArrayList()
        while (cursor.moveToNext())
        {
            val id = cursor.getLong(indexId)
            val emoji = cursor.getString(indexEmoji)
            val name = cursor.getString(indexName)
            folderItems.add(FolderItem(false, id, emoji, name, null))
        }
        cursor.close()
        return folderItems
    }

    fun deleteNote(id: Long) {
        writableDatabase.delete(TABLE_NAME_NOTES, "$ID_NOTES=$id", null)
    }

    fun deleteFolder(id: Long) {
        writableDatabase.delete(TABLE_NAME_FOLDERS, "$ID_FOLDERS=$id", null)
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
        contentValues.put(ID_FOLDERS, APNotepadConstants.ROOT_FOLDER_ID)
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
        const val ID_NOTES = "_id"
        const val EMOJI_NOTES = "emoji"
        const val TITLE_NOTES = "title"
        const val TEXT_NOTES = "text"
        const val TYPE_NOTES = "type"
        const val FOLDER_ID_NOTES = "folder_id"

        const val TABLE_NAME_FOLDERS = "folders"
        const val ID_FOLDERS = "_id"
        const val EMOJI_FOLDERS = "emoji"
        const val NAME_FOLDERS = "name"
        const val PARENT_ID_FOLDERS = "parent_id"

        const val DB_NAME = "AP_NOTEPAD.DB"
        const val DB_VERSION = 1

        private const val CREATE_TABLE_NOTES =
            "create table " + TABLE_NAME_NOTES + "(" +
                    ID_NOTES + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    EMOJI_NOTES + " TEXT, " +
                    TITLE_NOTES + " TEXT, " +
                    TEXT_NOTES + " TEXT, " +
                    TYPE_NOTES + " INTEGER NOT NULL, " +
                    FOLDER_ID_NOTES + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + FOLDER_ID_NOTES + ") REFERENCES " + TABLE_NAME_FOLDERS + "(" + ID_FOLDERS + ") ON DELETE CASCADE" +
                    ");"
        private const val CREATE_TABLE_FOLDERS =
            "create table " + TABLE_NAME_FOLDERS + "(" +
                    ID_FOLDERS + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    EMOJI_FOLDERS + " TEXT, " +
                    NAME_FOLDERS + " TEXT NOT NULL, " +
                    PARENT_ID_FOLDERS + " INTEGER, " +
                    "FOREIGN KEY (" + PARENT_ID_FOLDERS + ") REFERENCES " + TABLE_NAME_FOLDERS + "(" + ID_FOLDERS + ") ON DELETE CASCADE" +
                    ");"
    }
}