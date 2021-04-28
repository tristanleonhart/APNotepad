package dk.sdu.ap.apnotepad

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NoteUtils {
    companion object {
        private val gson = Gson()

        fun getNotePreview(type: Int, text: String) : String {
            var preview : String
            if (type == APNotepadConstants.NOTE_TYPE_CHECKLIST) {
                // create preview for a checklist note
                preview = ""
                val jsonType = object : TypeToken<ArrayList<TodoItem>>() {}.type
                for (todoItem in gson.fromJson<ArrayList<TodoItem>>(text, jsonType)) {
                    if (preview.isNotEmpty()) {
                        preview += ", "
                    }
                    // add a checkbox
                    preview += if (todoItem.checked) {
                        APNotepadConstants.CHECKBOX_CHECKED
                    } else {
                        APNotepadConstants.CHECKBOX_UNCHECKED
                    }
                    preview += " " + todoItem.text
                }
                if (preview.isEmpty()) {
                    preview = "Empty checklist"
                }
            } else {
                // create preview for a plaintext note
                preview = text
                if (preview.isEmpty()) {
                    preview = "Empty note"
                }
            }
            return preview
        }

        fun folderItemFromNote(note: Note) : FolderItem {
            val preview = getNotePreview(note.type, note.text)
            return FolderItem(true, note.id, note.emoji, note.title, preview)
        }
    }
}