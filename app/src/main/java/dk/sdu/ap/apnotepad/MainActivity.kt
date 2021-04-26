package dk.sdu.ap.apnotepad

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity(), FolderItemRecyclerViewAdapter.ItemClickListener {
    var databaseHelper: DatabaseHelper? = null

    private var path: ArrayList<Long> = ArrayList()
    private var current_note_position: Int? = null
    var current_folder_position: Int? = null

    val folderItems: ArrayList<FolderItem> = ArrayList()
    val adapter = FolderItemRecyclerViewAdapter(folderItems)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.create_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId === R.id.add_note || item.itemId === R.id.add_todo) {
            val intent = Intent(applicationContext, NoteEditorActivity::class.java)
            val type = if (item.itemId == R.id.add_todo) 2 else 1
            intent.putExtra("type", type)
            intent.putExtra("folderId", path.last())
            startActivityForResult(intent, 0)
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Uncomment line below to reset database on app startup
        //applicationContext.deleteDatabase(DatabaseHelper.DB_NAME)

        // get database
        databaseHelper = DatabaseHelper.getInstance(this)

        path.add(0)
        folderItems.addAll(databaseHelper!!.getFolderItems(path.last()))
        adapter.notifyDataSetChanged()

        // listen for item clicks
        adapter.setItemClickListener(this)

        // set up the RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.folderItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val noteCreated = intent.getBooleanExtra("noteCreated", false)
        if (noteCreated) {
            // Note was created
            val note_id = intent.getLongExtra("noteId", -1)
            val note = databaseHelper!!.getNote(note_id) as Note
            folderItems.add(NoteUtils.folderItemFromNote(note))
            adapter.notifyItemInserted(folderItems.size - 1)
        } else {
            // Note was updated
            val note_id = folderItems[current_note_position!!].id
            val note = databaseHelper!!.getNote(note_id) as Note
            folderItems[current_note_position!!] = NoteUtils.folderItemFromNote(note)
            adapter.notifyItemChanged(current_note_position!!)
            current_note_position = null
        }
    }

    override fun onBackPressed() {
        if (path.last() != 0.toLong()) {
            // go up one folder level
            path.removeLast()
            folderItems.clear()
            folderItems.addAll(databaseHelper!!.getFolderItems(path.last()))
            adapter.notifyDataSetChanged()
        } else {
            // normal back press behaviour
            super.onBackPressed()
        }
    }

    override fun onItemClick(position: Int) {
        if (folderItems[position].note) {
            // note was clicked (open it)
            current_note_position = position
            val note = databaseHelper!!.getNote(folderItems[position].id) as Note
            val intent = Intent(applicationContext, NoteEditorActivity::class.java)
            intent.putExtra("noteId", note.id)
            intent.putExtra("folderId", path.last())
            startActivityForResult(intent, 0)
        } else {
            // folder was clicked (show contents)
            val folder_id = folderItems[position].id
            // go down one folder level
            path.add(folder_id)
            folderItems.clear()
            folderItems.addAll(databaseHelper!!.getFolderItems(path.last()))
            adapter.notifyDataSetChanged()
        }
    }

    override fun onItemLongClick(position: Int) {
        if (folderItems[position].note) {
            // delete note dialog
            AlertDialog.Builder(this@MainActivity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete Note")
                .setMessage("Do you want to delete this note?")
                .setPositiveButton("Yes",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        databaseHelper!!.deleteNote(folderItems[position].id)
                        folderItems.removeAt(position)
                        adapter.notifyItemRemoved(position)
                    }).setNegativeButton("No", null).show()
        } else {
            // edit or delete folder dialog
            // todo
        }
    }
}