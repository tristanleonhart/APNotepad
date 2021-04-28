package dk.sdu.ap.apnotepad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vanniktech.emoji.EmojiTextView

class FolderItemRecyclerViewAdapter(private val folderItems: ArrayList<FolderItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemClickListener: ItemClickListener? = null

    inner class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val folderEmoji: EmojiTextView = view.findViewById(R.id.listFolderEmoji)
        val folderName: TextView = view.findViewById(R.id.listFolderName)

        init {
            view.setOnLongClickListener {
                itemClickListener?.onItemLongClick(adapterPosition)
                true
            }
            view.setOnClickListener {
                itemClickListener?.onItemClick(adapterPosition)
            }
        }
    }

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteEmoji: EmojiTextView = view.findViewById(R.id.listNoteEmoji)
        val noteTitle: TextView = view.findViewById(R.id.listNoteTitle)
        val notePreview: TextView = view.findViewById(R.id.listNotePreview)

        init {
            view.setOnLongClickListener {
                itemClickListener?.onItemLongClick(adapterPosition)
                true
            }
            view.setOnClickListener {
                itemClickListener?.onItemClick(adapterPosition)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (folderItems[position].isNote) {
            APNotepadConstants.FOLDER_ITEM_TYPE_NOTE
        } else {
            APNotepadConstants.FOLDER_ITEM_TYPE_FOLDER
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == APNotepadConstants.FOLDER_ITEM_TYPE_NOTE) {
            // return a NoteViewHolder
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.folder_item_note, viewGroup, false)
            NoteViewHolder(view)
        } else {
            // return a FolderViewHolder
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.folder_item_folder, viewGroup, false)
            FolderViewHolder(view)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder.itemViewType == APNotepadConstants.FOLDER_ITEM_TYPE_NOTE) {
            // bind to a NoteViewHolder
            val noteViewHolder = viewHolder as NoteViewHolder
            noteViewHolder.noteEmoji.text = folderItems[position].emoji
            val title = folderItems[position].name
            if (title.isEmpty()) {
                noteViewHolder.noteTitle.text = APNotepadConstants.UNTITLED_PLACEHOLDER
            } else {
                noteViewHolder.noteTitle.text = folderItems[position].name
            }
            noteViewHolder.notePreview.text = folderItems[position].preview
        } else {
            // bind to a FolderViewHolder
            val folderViewHolder = viewHolder as FolderViewHolder
            folderViewHolder.folderEmoji.text = folderItems[position].emoji
            folderViewHolder.folderName.text = folderItems[position].name
            val title = folderItems[position].name
            if (title.isEmpty()) {
                folderViewHolder.folderName.text = APNotepadConstants.UNTITLED_PLACEHOLDER
            } else {
                folderViewHolder.folderName.text = folderItems[position].name
            }
        }
    }

    override fun getItemCount() = folderItems.size

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(position: Int)
        fun onItemLongClick(position: Int)
    }
}
