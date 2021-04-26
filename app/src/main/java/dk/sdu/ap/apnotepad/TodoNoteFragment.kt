package dk.sdu.ap.apnotepad

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
    private val todoItems: ArrayList<TodoItem> = ArrayList()
    private val adapter = TodoItemRecyclerViewAdapter(todoItems)
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_todo_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val jsonStr = (activity as NoteEditorActivity).note!!.text
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
        (activity as NoteEditorActivity).note!!.text = gson.toJson(todoItems)
    }

    override fun onItemChanged(position: Int, item: TodoItem) {
        todoItems[position] = item
        saveTodoList()
    }
}