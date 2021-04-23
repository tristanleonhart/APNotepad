package dk.sdu.ap.apnotepad

import android.content.Context
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
    var noteId = 0
    var type_todo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(IosEmojiProvider())
        setContentView(R.layout.activity_note_editor)

        noteId = intent.getIntExtra("noteId", -1)
        type_todo = intent.getBooleanExtra("type_todo", false)
        if (noteId == -1) {
            // Create a new note
            MainActivity.notes.add(if (type_todo) "[]" else "")
            MainActivity.emojis.add(String(Character.toChars(0x1F60A)))
            noteId = MainActivity.notes.size - 1
            MainActivity.arrayAdapter?.notifyDataSetChanged()
            val sharedPreferences = applicationContext.getSharedPreferences(
                "dk.sdu.ap.apnotepad",
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().putStringSet("notes", HashSet(MainActivity.notes)).apply()
            sharedPreferences.edit().putStringSet("emojis", HashSet(MainActivity.emojis)).apply()
        }

        val title = findViewById<TextView>(R.id.noteTitle)
        title.text = "Note Title" // Todo

        setUpEmojiPopup()

        // Insert the note fragment
        val ft = supportFragmentManager.beginTransaction()
        if (type_todo)
        {
            ft.replace(R.id.noteFragmentPlaceholder, TodoNoteFragment.newInstance(noteId))
        }
        else
        {
            ft.replace(R.id.noteFragmentPlaceholder, TextNoteFragment.newInstance(noteId))
        }
        ft.commit()
    }

    private fun setUpEmojiPopup() {
        val noteEmoji = findViewById<EmojiEditText>(R.id.noteEmoji)
        noteEmoji.isCursorVisible = false
        noteEmoji.inputType = InputType.TYPE_NULL
        noteEmoji.setBackgroundResource(android.R.color.transparent)
        noteEmoji.setText(MainActivity.emojis[noteId])
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
                MainActivity.emojis[noteId] = noteEmoji.text.toString()
                val sharedPreferences = applicationContext.getSharedPreferences(
                    "dk.sdu.ap.apnotepad",
                    Context.MODE_PRIVATE
                )
                sharedPreferences.edit().putStringSet("emojis", HashSet(MainActivity.emojis))
                    .apply()
            }

            override fun afterTextChanged(s: Editable) {
                noteEmoji.setSelection(noteEmoji.text.toString().length)
            }
        })
    }
}