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
        return if (folderItems[position].note) 0 else 1
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            // Folder
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.folder_item_folder, viewGroup, false)
            FolderViewHolder(view)
        } else {
            // Note
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.folder_item_note, viewGroup, false)
            NoteViewHolder(view)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder.itemViewType == 1) {
            // Folder
            val folderViewHolder = viewHolder as FolderViewHolder
            folderViewHolder.folderEmoji.text = folderItems[position].emoji
            folderViewHolder.folderName.text = folderItems[position].name
        } else {
            // Note
            val noteViewHolder = viewHolder as NoteViewHolder
            noteViewHolder.noteEmoji.text = folderItems[position].emoji
            noteViewHolder.noteTitle.text = folderItems[position].name
            noteViewHolder.notePreview.text = folderItems[position].preview
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
