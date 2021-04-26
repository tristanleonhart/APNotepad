package dk.sdu.ap.apnotepad

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.vanniktech.emoji.EmojiEditText
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiUtils

class FolderEditDialog private constructor(val activity: MainActivity, val folder_id: Long) : DialogFragment() {

    private var editFolderRootView : ViewGroup? = null
    private var editFolderDialogEmoji : EmojiEditText? = null
    private var editFolderDialogName : EditText? = null

    companion object {
        fun showDialog(activity: MainActivity, folder_id: Long) {
            FolderEditDialog(activity, folder_id).show(activity.supportFragmentManager, null)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Edit or Delete Folder")
        builder.setView(buildView())
        builder.setPositiveButton("Save", object: DialogInterface.OnClickListener {
            override fun onClick(dialog:DialogInterface, which:Int) {
                val emoji = editFolderDialogEmoji!!.text.toString()
                val name = editFolderDialogName!!.text.toString()
                activity.databaseHelper!!.updateFolder(folder_id, emoji, name)
                activity.folderItems[activity.current_folder_position!!].emoji = emoji
                activity.folderItems[activity.current_folder_position!!].name = name
                activity.adapter.notifyItemChanged(activity.current_folder_position!!)
                activity.current_folder_position = null
                dismiss()
            }
        })
        builder.setNegativeButton("Delete Folder", object: DialogInterface.OnClickListener {
            override fun onClick(dialog:DialogInterface, which:Int) {
                activity.databaseHelper!!.deleteFolder(folder_id)
                activity.adapter.notifyItemRemoved(activity.current_folder_position!!)
                activity.current_folder_position = null
                dismiss()
            }
        })
        return builder.create()
    }

    private fun buildView(): View? {
        val view = View.inflate(context, R.layout.folder_edit_dialog, null)
        editFolderDialogName = view.findViewById(R.id.editFolderDialogName)
        editFolderDialogEmoji = view.findViewById(R.id.editFolderDialogEmoji)
        editFolderRootView = view.findViewById(R.id.edit_folder_dialog_root_view)
        setUpEmojiPopup()
        return editFolderRootView
    }

    private fun setUpEmojiPopup() {
        editFolderDialogEmoji!!.isCursorVisible = false
        editFolderDialogEmoji!!.inputType = InputType.TYPE_NULL
        editFolderDialogEmoji!!.setBackgroundResource(android.R.color.transparent)
        val emoji = activity.folderItems[activity.current_folder_position!!].emoji
        editFolderDialogEmoji!!.setText(emoji)
        val emojiPopup = EmojiPopup.Builder.fromRootView(editFolderRootView)
            .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
            .setOnEmojiBackspaceClickListener { editFolderDialogEmoji!!.setText(String(Character.toChars(0x1F60A))) }
            .build(editFolderDialogEmoji!!)
        editFolderDialogEmoji!!.disableKeyboardInput(emojiPopup)
        editFolderDialogEmoji!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editFolderDialogEmoji!!.removeTextChangedListener(this)
                var emoji = s.subSequence(start, start + count)
                if (emoji.isEmpty() || !EmojiUtils.isOnlyEmojis(emoji) || EmojiUtils.emojisCount(
                        emoji
                    ) != 1
                ) {
                    editFolderDialogEmoji!!.setText(String(Character.toChars(0x1F60A)))
                } else {
                    editFolderDialogEmoji!!.setText(emoji);
                }
                editFolderDialogEmoji!!.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable) {
                editFolderDialogEmoji!!.setSelection(editFolderDialogEmoji!!.text.toString().length)
            }
        })
    }
}