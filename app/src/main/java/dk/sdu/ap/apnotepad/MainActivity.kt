package dk.sdu.ap.apnotepad

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cafe.adriel.krumbsview.KrumbsView
import cafe.adriel.krumbsview.model.Krumb
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider

class MainActivity : AppCompatActivity(), FolderItemRecyclerViewAdapter.ItemClickListener {
    lateinit var databaseHelper: DatabaseHelper

    private lateinit var placeholder : ViewGroup
    private lateinit var breadcrumbs : KrumbsView

    var path: MutableList<Long> = ArrayList()

    var currentItemIdx: Int? = null
    val folderItems: ArrayList<FolderItem> = ArrayList()
    val adapter = FolderItemRecyclerViewAdapter(folderItems)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // initialize menu to create notes and folders
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.create_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.add_note_plaintext || item.itemId == R.id.add_note_checklist) {
            // create a note (plaintext or checklist)
            val intent = Intent(applicationContext, NoteEditorActivity::class.java)
            val type = if (item.itemId == R.id.add_note_plaintext) {
                APNotepadConstants.NOTE_TYPE_PLAINTEXT
            } else {
                APNotepadConstants.NOTE_TYPE_CHECKLIST
            }
            intent.putExtra("type", type)
            intent.putExtra("folderId", path.last())
            // start the note editor activity
            startActivityForResult(intent, 0)
            return true
        } else if (item.itemId == R.id.add_folder) {
            // create a folder or subfolder
            FolderEditDialog.showDialog(this, -1)
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enable the emoji layout components
        EmojiManager.install(IosEmojiProvider())
        setContentView(R.layout.activity_main)

        // get placeholder for empty RecyclerView
        placeholder = findViewById(R.id.folderItemsPlaceholder)

        // configure breadcrumbs view
        breadcrumbs = findViewById(R.id.breadcrumbs)
        breadcrumbs.setOnPreviousItemClickListener {
            // go up one folder level
            folderLevelUp(false)
        }

        // uncomment line below to reset database on app startup
        //applicationContext.deleteDatabase(DatabaseHelper.DB_NAME)

        // get database
        databaseHelper = DatabaseHelper.getInstance(this)

        // check if a saved path is available
        val savedPath = savedInstanceState?.getLongArray("path")
        if (savedPath != null) {
            // restore the path to the previous folder
            path = savedPath.toMutableList()
        } else {
            // set the path to the root folder
            path.clear()
            path.add(APNotepadConstants.ROOT_FOLDER_ID)
        }

        // load the items inside the current folder
        folderItems.clear()
        folderItems.addAll(databaseHelper.getFolderItems(path.last()))
        adapter.notifyDataSetChanged()

        // listen for item clicks
        adapter.setItemClickListener(this)
        // show a placeholder view if there are no items to display
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            init {
                checkEmpty()
            }

            override fun onChanged() {
                super.onChanged()
                checkEmpty()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmpty()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmpty()
            }

            private fun checkEmpty() {
                val isEmpty = adapter.itemCount == 0
                placeholder.visibility = if (isEmpty) View.VISIBLE else View.GONE
            }
        })

        // set up the RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.folderItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intent = data as Intent
        if (intent.getBooleanExtra("noteCreated", false)) {
            // a note was created
            val noteId = intent.getLongExtra("noteId", -1)
            val note = databaseHelper.getNote(noteId)
            val index = getInsertNoteIndex()
            folderItems.add(index, NoteUtils.folderItemFromNote(note))
            adapter.notifyItemInserted(index)
        } else {
            // a note was updated
            val noteId = folderItems[currentItemIdx!!].id
            val note = databaseHelper.getNote(noteId)
            folderItems[currentItemIdx!!] = NoteUtils.folderItemFromNote(note)
            adapter.notifyItemChanged(currentItemIdx!!)
            currentItemIdx = null
        }
    }

    override fun onBackPressed() {
        if (path.last() != APNotepadConstants.ROOT_FOLDER_ID) {
            // go up one folder level
            folderLevelUp(true)
        } else {
            // we are at the root folder level (normal back press behaviour)
            super.onBackPressed()
        }
    }

    private fun folderLevelUp(updateBreadcrumbs: Boolean) {
        path.removeLast()
        if (updateBreadcrumbs) {
            breadcrumbs.removeLastItem()
        }
        folderItems.clear()
        folderItems.addAll(databaseHelper.getFolderItems(path.last()))
        adapter.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLongArray("path", path.toLongArray())
    }

    override fun onItemClick(position: Int) {
        if (folderItems[position].isNote) {
            // a note was clicked (open it)
            currentItemIdx = position
            val note = databaseHelper.getNote(folderItems[position].id)
            val intent = Intent(applicationContext, NoteEditorActivity::class.java)
            intent.putExtra("noteId", note.id)
            intent.putExtra("folderId", path.last())
            // start the note editor activity
            startActivityForResult(intent, 0)
        } else {
            // a folder was clicked (show its contents)
            val folderId = folderItems[position].id
            // go down one folder level
            path.add(folderId)
            val folder = databaseHelper.getFolder(folderId)
            val breadcrumbText = if (folder.name.isEmpty()) {
                APNotepadConstants.UNTITLED_PLACEHOLDER
            } else {
                folder.name
            }
            breadcrumbs.addItem(Krumb(breadcrumbText))
            folderItems.clear()
            folderItems.addAll(databaseHelper.getFolderItems(path.last()))
            adapter.notifyDataSetChanged()
        }
    }

    override fun onItemLongClick(position: Int) {
        if (folderItems[position].isNote) {
            // delete note dialog
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Delete Note")
                .setMessage("Do you want to delete this note?")
                .setPositiveButton("Yes") { _, _ ->
                    databaseHelper.deleteNote(folderItems[position].id)
                    folderItems.removeAt(position)
                    adapter.notifyItemRemoved(position)
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            // edit or delete folder dialog
            currentItemIdx = position
            FolderEditDialog.showDialog(this, folderItems[position].id)
        }
    }

    private fun getInsertNoteIndex(): Int {
        var index = 0
        for (folderItem in folderItems) {
            if (folderItem.isNote) {
                break
            }
            index++
        }
        return index
    }
}