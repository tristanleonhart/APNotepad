package dk.sdu.ap.apnotepad

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class NoteEditorActivity : AppCompatActivity() {
    var noteId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        val title = findViewById<TextView>(R.id.noteTitle)
        val body = findViewById<TextView>(R.id.noteBody)

        noteId = intent.getIntExtra("noteId", -1)
        if (noteId != -1) {
            // Get a pre existing note
            body.setText(MainActivity.notes.get(noteId))
        } else {
            // Create a new note
            MainActivity.notes.add("")
            noteId = MainActivity.notes.size - 1
            MainActivity.arrayAdapter?.notifyDataSetChanged()
        }

        body.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                MainActivity.notes.set(noteId, charSequence.toString())
                MainActivity.arrayAdapter?.notifyDataSetChanged()
                val sharedPreferences = applicationContext.getSharedPreferences("dk.sdu.ap.apnotepad.notes", Context.MODE_PRIVATE)
                val set: HashSet<String> = HashSet(MainActivity.notes)
                sharedPreferences.edit().putStringSet("notes", set).apply()
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }
}