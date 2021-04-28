package dk.sdu.ap.apnotepad

import cafe.adriel.krumbsview.model.Krumb

data class Folder(var id: Long, var emoji: String, var name: String, var parent_id: Long) : Krumb(name)
