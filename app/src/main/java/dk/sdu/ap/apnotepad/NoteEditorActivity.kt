package dk.sdu.ap.apnotepad

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vanniktech.emoji.EmojiEditText
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiUtils
import com.vanniktech.emoji.ios.IosEmojiProvider


class NoteEditorActivity : AppCompatActivity() {
    private var databaseHelper: DatabaseHelper? = null

    var noteCreated : Boolean = false
    var noteId : Long = 0
    var folderId : Long = 0
    var note : Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(IosEmojiProvider())
        setContentView(R.layout.activity_note_editor)

        // get database
        databaseHelper = DatabaseHelper.getInstance(this)

        noteId = intent.getLongExtra("noteId", -1)
        folderId = intent.getLongExtra("folderId", -1)

        if (noteId == (-1).toLong()) {
            // Create a new note
            noteCreated = true
            val type = intent.getIntExtra("type", 1)
            val emoji = String(Character.toChars(0x1F60A))
            val title = "Note Title"
            val text = if (type == 1) "" else "[]"
            note = Note(-1, type, emoji, title, text)
            noteId = databaseHelper!!.insertNote(note!!, folderId)
        } else {
            note = databaseHelper!!.getNote(noteId)
        }

        val title = findViewById<TextView>(R.id.noteTitle)
        title.text = note!!.title

        setUpEmojiPopup()

        // Insert the note fragment
        val ft = supportFragmentManager.beginTransaction()
        if (note!!.type == 1)
        {
            ft.replace(R.id.noteFragmentPlaceholder, TextNoteFragment())
        }
        else
        {
            ft.replace(R.id.noteFragmentPlaceholder, TodoNoteFragment())
        }
        ft.commit()
    }

    override fun onPause() {
        super.onPause()
        databaseHelper!!.updateNote(note as Note)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent()
        intent.putExtra("noteCreated", noteCreated)
        intent.putExtra("noteId", noteId)
        setResult(RESULT_OK, intent)
    }

    private fun setUpEmojiPopup() {
        val noteEmoji = findViewById<EmojiEditText>(R.id.noteEmoji)
        noteEmoji.isCursorVisible = false
        noteEmoji.inputType = InputType.TYPE_NULL
        noteEmoji.setBackgroundResource(android.R.color.transparent)
        noteEmoji.setText(note!!.emoji)
        val emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.rootView))
            .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
            .setOnEmojiBackspaceClickListener { noteEmoji.setText(String(Character.toChars(0x1F60A))) }
            .build(noteEmoji)
        noteEmoji.disableKeyboardInput(emojiPopup)
        noteEmoji.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                noteEmoji.removeTextChangedListener(this)
                var emoji = s.subSequence(start, start + count)
                if (emoji.isEmpty() || !EmojiUtils.isOnlyEmojis(emoji) || EmojiUtils.emojisCount(
                        emoji
                    ) != 1
                ) {
                    noteEmoji.setText(String(Character.toChars(0x1F60A)))
                } else {
                    noteEmoji.setText(emoji);
                }
                noteEmoji.addTextChangedListener(this)
                // Store emoji
                note!!.emoji = noteEmoji.text.toString()
            }

            override fun afterTextChanged(s: Editable) {
                noteEmoji.setSelection(noteEmoji.text.toString().length)
            }
        })
    }
}