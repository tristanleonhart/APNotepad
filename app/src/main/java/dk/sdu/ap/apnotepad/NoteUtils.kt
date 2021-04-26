package dk.sdu.ap.apnotepad

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NoteUtils {
    companion object {
        private val gson = Gson()

        fun getNotePreview(note: Note) : String {
            return getNotePreview(note.type, note.text)
        }

        fun getNotePreview(type: Int, text: String) : String {
            var preview : String
            if (type == 2) {
                preview = ""
                val jsonType = object: TypeToken<ArrayList<TodoItem>>(){}.type
                for (todoItem in gson.fromJson<ArrayList<TodoItem>>(text, jsonType)) {
                    if (preview.isNotEmpty()) {
                        preview += ", "
                    }
                    preview += if (todoItem.checked) 0x2611.toChar() else 0x2610.toChar()
                    preview += " " + todoItem.text
                }
            } else {
                preview = text
            }
            return if (preview.length <= 100) {
                preview
            } else {
                preview.take(100) + "..."
            }
        }

        fun folderItemFromNote(note: Note) : FolderItem {
            val preview = getNotePreview(note)
            return FolderItem(true, note.id, note.emoji, note.title, preview)
        }
    }
}