package dk.sdu.ap.apnotepad

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView

class TodoItemRecyclerViewAdapter(private val todoItems: ArrayList<TodoItem>) :
    RecyclerView.Adapter<TodoItemRecyclerViewAdapter.ViewHolder>() {

    private var itemChangedListener: ItemChangedListener? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), TextWatcher {
        val todoItemText: EditText = view.findViewById(R.id.todoItemText)
        val todoItemCheckBox: CheckBox = view.findViewById(R.id.todoItemCheckBox)

        init {
            todoItemText.addTextChangedListener(this)
            todoItemCheckBox.setOnClickListener {
                val checked = todoItemCheckBox.isChecked
                val text = todoItemText.text.toString()
                itemChanged(adapterPosition, checked, text)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // do nothing
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val checked = todoItemCheckBox.isChecked
            val text = todoItemText.text.toString()
            itemChanged(adapterPosition, checked, text)
        }

        override fun afterTextChanged(s: Editable?) {
            // do nothing
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.todo_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.todoItemCheckBox.isChecked = todoItems[position].checked
        viewHolder.todoItemText.setText(todoItems[position].text)
    }

    override fun getItemCount() = todoItems.size

    private fun itemChanged(position: Int, checked: Boolean, text: String) {
        val item = TodoItem(checked, text)
        itemChangedListener?.onItemChanged(position, item)
    }

    fun setItemChangedListener(itemChangedListener: ItemChangedListener) {
        this.itemChangedListener = itemChangedListener
    }

    fun interface ItemChangedListener {
        fun onItemChanged(position: Int, item: TodoItem)
    }
}
