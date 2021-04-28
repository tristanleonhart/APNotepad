package dk.sdu.ap.apnotepad

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class TextNoteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val body = view.findViewById<TextView>(R.id.noteBody)
        body.text = (activity as NoteEditorActivity).note.text
        body.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                (activity as NoteEditorActivity).note.text = s.toString()
            }

            override fun afterTextChanged(s: Editable) {
                // do nothing
            }
        })
    }
}