package dk.sdu.ap.apnotepad

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.vanniktech.emoji.EmojiEditText
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiUtils
import com.vanniktech.emoji.ios.IosEmojiProvider

class NoteEditorActivity : AppCompatActivity() {
    private lateinit var databaseHelper: DatabaseHelper

    private var noteCreated: Boolean = false

    private var folderId: Long = -1
    lateinit var note: Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enable the emoji layout components
        EmojiManager.install(IosEmojiProvider())
        setContentView(R.layout.activity_note_editor)

        // get database
        databaseHelper = DatabaseHelper.getInstance(this)

        // get the id of the folder that this note is in
        folderId = intent.getLongExtra("folderId", -1)

        val noteId = intent.getLongExtra("noteId", -1)
        if (noteId == (-1).toLong()) {
            // create a new note
            noteCreated = true
            val type = intent.getIntExtra("type", APNotepadConstants.NOTE_TYPE_PLAINTEXT)
            val emoji = APNotepadConstants.DEFAULT_EMOJI
            val title = ""
            val text = if (type == APNotepadConstants.NOTE_TYPE_PLAINTEXT) "" else "[]"
            note = Note(-1, type, emoji, title, text)
            note.id = databaseHelper.insertNote(note, folderId)
        } else {
            note = databaseHelper.getNote(noteId)
        }

        val title = findViewById<EditText>(R.id.noteTitle)
        title.setText(note.title)
        title.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                note.title = s.toString()
            }

            override fun afterTextChanged(s: Editable) {
                // do nothing
            }
        })

        setUpEmojiPopup()

        // insert the note fragment
        val ft = supportFragmentManager.beginTransaction()
        if (note.type == APNotepadConstants.NOTE_TYPE_PLAINTEXT) {
            ft.replace(R.id.noteFragmentPlaceholder, TextNoteFragment())
        } else {
            ft.replace(R.id.noteFragmentPlaceholder, TodoNoteFragment())
        }
        ft.commit()
    }

    override fun onPause() {
        super.onPause()
        // save changes
        databaseHelper.updateNote(note)
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("noteCreated", noteCreated)
        intent.putExtra("noteId", note.id)
        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }

    private fun setUpEmojiPopup() {
        val noteEmoji = findViewById<EmojiEditText>(R.id.noteEmoji)
        noteEmoji.isCursorVisible = false
        noteEmoji.inputType = InputType.TYPE_NULL
        noteEmoji.setBackgroundResource(android.R.color.transparent)
        noteEmoji.setText(note.emoji)
        val emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.noteEditorRootView))
            .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
            .setOnEmojiBackspaceClickListener {
                noteEmoji.setText(APNotepadConstants.DEFAULT_EMOJI)
            }
            .build(noteEmoji)
        noteEmoji.disableKeyboardInput(emojiPopup)
        noteEmoji.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                noteEmoji.removeTextChangedListener(this)
                val emoji = s.subSequence(start, start + count)
                val validInput = emoji.isNotEmpty() && EmojiUtils.isOnlyEmojis(emoji) &&
                        EmojiUtils.emojisCount(emoji) == 1
                if (validInput) {
                    noteEmoji.setText(emoji)
                } else {
                    noteEmoji.setText(APNotepadConstants.DEFAULT_EMOJI)
                }
                noteEmoji.addTextChangedListener(this)
                // store the emoji
                note.emoji = noteEmoji.text.toString()
            }

            override fun afterTextChanged(s: Editable) {
                noteEmoji.setSelection(noteEmoji.text.toString().length)
            }
        })
    }
}