package dk.sdu.ap.apnotepad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TodoNoteFragment : Fragment(), TodoItemRecyclerViewAdapter.ItemChangedListener,
    TodoItemRecyclerViewAdapter.ItemLongClickListener {

    private lateinit var placeholder : ViewGroup

    private var openDialog : AlertDialog? = null
    private var restoreDeleteItemDialog = false

    private var currentItemIdx : Int = -1
    private val todoItems : ArrayList<TodoItem> = ArrayList()
    private val adapter = TodoItemRecyclerViewAdapter(todoItems)

    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_todo_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get placeholder for empty RecyclerView
        placeholder = view.findViewById(R.id.todoItemsPlaceholder)

        // restore the current item index
        currentItemIdx = savedInstanceState?.getInt("currentItemIdx", -1) ?: -1

        // restore open dialogs
        if (savedInstanceState?.getBoolean("restoreDeleteItemDialog", false) == true) {
            showDeleteItemDialog()
        }

        val jsonStr = (activity as NoteEditorActivity).note.text
        if (jsonStr.isNotEmpty()) {
            val jsonType = object : TypeToken<ArrayList<TodoItem>>() {}.type
            todoItems.addAll(gson.fromJson<ArrayList<TodoItem>>(jsonStr, jsonType))
            adapter.notifyDataSetChanged()
        }

        // listen for item changes
        adapter.setItemChangedListener(this)
        // listen for item long clicks
        adapter.setItemLongClickListener(this)
        // show a placeholder view if there are no items to display
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            init {
                checkEmpty()
            }

            override fun onChanged() {
                super.onChanged()
                checkEmpty()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmpty()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmpty()
            }

            private fun checkEmpty() {
                val isEmpty = adapter.itemCount == 0
                placeholder.visibility = if (isEmpty) View.VISIBLE else View.GONE
            }
        })

        // set up the RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.todoItems)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        // set up the add item FAB
        val addFab = view.findViewById<FloatingActionButton>(R.id.addFab)
        addFab.setOnClickListener { addNewTodoItem() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentItemIdx", currentItemIdx)
        outState.putBoolean("restoreDeleteItemDialog", restoreDeleteItemDialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        openDialog?.dismiss()
    }

    private fun addNewTodoItem() {
        val item = TodoItem(false, "")
        todoItems.add(0, item)
        adapter.notifyItemInserted(0)
        saveTodoList()
    }

    private fun saveTodoList() {
        (activity as NoteEditorActivity).note.text = gson.toJson(todoItems)
    }

    override fun onItemChanged(position: Int, item: TodoItem) {
        todoItems[position] = item
        saveTodoList()
    }

    override fun onItemLongClick(position: Int) {
        // save current item index
        currentItemIdx = position
        // show delete item dialog
        showDeleteItemDialog()
    }

    private fun showDeleteItemDialog() {
        restoreDeleteItemDialog = true
        openDialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Do you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                todoItems.removeAt(currentItemIdx)
                adapter.notifyItemRemoved(currentItemIdx)
                saveTodoList()
            }
            .setNegativeButton("No", null)
            .setOnDismissListener {
                currentItemIdx = -1
                restoreDeleteItemDialog = false
                openDialog = null
            }
            .show()
    }
}