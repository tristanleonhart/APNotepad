package dk.sdu.ap.apnotepad

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class TodoNoteFragment : Fragment(), TodoItemRecyclerViewAdapter.ItemChangedListener {
    private var noteId: Int = 0
    private val todoItems: ArrayList<TodoItem> = ArrayList()
    private val adapter = TodoItemRecyclerViewAdapter(todoItems)
    private val gson = Gson()

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

        val jsonStr = MainActivity.notes[noteId]
        if (jsonStr.isNotEmpty())
        {
            val jsonType = object: TypeToken<ArrayList<TodoItem>>(){}.type
            todoItems.addAll(gson.fromJson<ArrayList<TodoItem>>(jsonStr, jsonType))
            adapter.notifyDataSetChanged()
        }

        // listen for item changes
        adapter.setItemChangedListener(this)

        // set up the RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.todoItems)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        // set up add FAB
        val addFab = view.findViewById<FloatingActionButton>(R.id.addFab)
        addFab.setOnClickListener { addNewTodoItem() }
    }

    private fun addNewTodoItem() {
        val item = TodoItem(false, "")
        todoItems.add(0, item)
        adapter.notifyItemInserted(0)
        saveTodoList()
    }

    private fun saveTodoList() {
        MainActivity.notes[noteId] = gson.toJson(todoItems)
        MainActivity.arrayAdapter?.notifyDataSetChanged()
        val sharedPreferences = activity?.applicationContext?.getSharedPreferences(
            "dk.sdu.ap.apnotepad",
            Context.MODE_PRIVATE
        )
        sharedPreferences?.edit()?.putStringSet("notes", HashSet(MainActivity.notes))
            ?.apply()
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

    override fun onItemChanged(position: Int, item: TodoItem) {
        todoItems[position] = item
        saveTodoList()
    }
}