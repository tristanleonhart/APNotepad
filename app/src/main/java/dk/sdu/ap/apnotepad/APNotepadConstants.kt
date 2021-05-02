package dk.sdu.ap.apnotepad

class APNotepadConstants {
    companion object {
        // note types
        const val NOTE_TYPE_PLAINTEXT = 1
        const val NOTE_TYPE_CHECKLIST = 2
        // folder item types
        const val FOLDER_ITEM_TYPE_NOTE = 0
        const val FOLDER_ITEM_TYPE_FOLDER = 1
        // database constants
        const val ROOT_FOLDER_ID : Long = 0
        const val AUTO_INCREMENT_ID : Long = -1
        // default emoji
        val DEFAULT_EMOJI = String(Character.toChars(0x1F60A))
        // checkboxes
        const val CHECKBOX_CHECKED = 0x2611.toChar()
        const val CHECKBOX_UNCHECKED = 0x2610.toChar()
        // default strings
        const val UNTITLED_PLACEHOLDER = "Untitled"
        // request codes
        const val REQUEST_CODE_PERMISSIONS_IMPORT = 1
        const val REQUEST_CODE_EDIT_NOTE = 2
        const val REQUEST_CODE_IMPORT_NOTES = 3
    }
}