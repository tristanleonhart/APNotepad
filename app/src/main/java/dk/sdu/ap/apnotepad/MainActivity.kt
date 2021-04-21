package dk.sdu.ap.apnotepad

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.create_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId === R.id.add_note) {
            val intent = Intent(applicationContext, NoteEditorActivity::class.java)
            startActivity(intent)
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val notesList: ListView = findViewById(R.id.notesList)

        val sharedPreferences =
            applicationContext.getSharedPreferences(
                "dk.sdu.ap.apnotepad.notes",
                Context.MODE_PRIVATE
            )
        val set = sharedPreferences.getStringSet("notes", null) as HashSet<String>?

        if (set == null) {
            notes.add("Example note")
        } else {
            notes = ArrayList(set)
        }

        arrayAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, notes)
        notesList.adapter = arrayAdapter

        notesList.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(applicationContext, NoteEditorActivity::class.java)
            intent.putExtra("noteId", i)
            startActivity(intent)
        }

        notesList.onItemLongClickListener = OnItemLongClickListener { adapterView, view, i, l ->
            val itemToDelete = i
            AlertDialog.Builder(this@MainActivity)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete Note")
                .setMessage("Do you want to delete this note?")
                .setPositiveButton("Yes",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        notes.removeAt(itemToDelete)
                        arrayAdapter!!.notifyDataSetChanged()
                        val sharedPreferences = applicationContext.getSharedPreferences(
                            "dk.sdu.ap.apnotepad.notes",
                            Context.MODE_PRIVATE
                        )
                        val set: HashSet<String> = HashSet(notes)
                        sharedPreferences.edit().putStringSet("notes", set).apply()
                    }).setNegativeButton("No", null).show()
            true
        }
    }

    companion object {
        var notes: ArrayList<String> = ArrayList()
        var arrayAdapter: ArrayAdapter<String>? = null
    }
}