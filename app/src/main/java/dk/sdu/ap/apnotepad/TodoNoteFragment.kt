package dk.sdu.ap.apnotepad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class TodoNoteFragment : Fragment() {
    private var noteId: Int = 0
    private lateinit var adapter: TodoItemRecyclerViewAdapter

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
        return inflater.inflate(R.layout.fragment_todo_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // data to populate the RecyclerView with
        val animalNames: ArrayList<String> = ArrayList()
        animalNames.add("Horse")
        animalNames.add("Cow")
        animalNames.add("Camel")
        animalNames.add("Sheep")
        animalNames.add("Goat")
        // set up the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.todoItems)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = TodoItemRecyclerViewAdapter(animalNames)
        recyclerView.adapter = adapter
    }

    companion object {

        @JvmStatic
        fun newInstance(noteId: Int) =
            TodoNoteFragment().apply {
                arguments = Bundle().apply {
                    putInt("noteId", noteId)
                }
            }
    }
}