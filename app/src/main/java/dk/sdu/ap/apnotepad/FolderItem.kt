package dk.sdu.ap.apnotepad

data class FolderItem(var isNote: Boolean, var id: Long, var emoji: String, var name: String, var preview: String?) {

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is FolderItem) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
