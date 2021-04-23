package dk.sdu.ap.apnotepad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView

class TodoItemRecyclerViewAdapter(private val todoItemsJson: ArrayList<String>) :
    RecyclerView.Adapter<TodoItemRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val todoItemText: EditText
        val todoItemCheckBox: CheckBox

        init {
            todoItemText = view.findViewById(R.id.todoItemText)
            todoItemCheckBox = view.findViewById(R.id.todoItemCheckBox)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.todo_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.todoItemText.setText(todoItemsJson[position])
    }

    override fun getItemCount() = todoItemsJson.size
}
