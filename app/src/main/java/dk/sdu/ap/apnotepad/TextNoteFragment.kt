package dk.sdu.ap.apnotepad

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class TextNoteFragment : Fragment() {
    private var noteId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noteId = it.getInt("noteId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val body = view.findViewById<TextView>(R.id.noteBody)
        body.text = MainActivity.notes[noteId]
        body.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                MainActivity.notes[noteId] = s.toString()
                MainActivity.arrayAdapter?.notifyDataSetChanged()
                val sharedPreferences = activity?.applicationContext?.getSharedPreferences(
                    "dk.sdu.ap.apnotepad",
                    Context.MODE_PRIVATE
                )
                sharedPreferences?.edit()?.putStringSet("notes", HashSet(MainActivity.notes))
                    ?.apply()
            }

            override fun afterTextChanged(s: Editable) {
                // do nothing
            }
        })
    }

    companion object {

        @JvmStatic
        fun newInstance(noteId: Int) =
            TextNoteFragment().apply {
                arguments = Bundle().apply {
                    putInt("noteId", noteId)
                }
            }
    }
}