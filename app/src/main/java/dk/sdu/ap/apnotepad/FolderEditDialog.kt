package dk.sdu.ap.apnotepad

import android.app.Dialog
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

class FolderEditDialog private constructor(private val activity: MainActivity, private val folder_id: Long) : DialogFragment() {
    private lateinit var editFolderRootView : ViewGroup
    private lateinit var editFolderDialogEmoji : EmojiEditText
    private lateinit var editFolderDialogName : EditText

    companion object {
        fun showDialog(activity: MainActivity, folder_id: Long) {
            // create and show the dialog
            FolderEditDialog(activity, folder_id).show(activity.supportFragmentManager, null)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        if (folder_id == (-1).toLong()) {
            builder.setTitle("Create Folder")
        } else {
            builder.setTitle("Edit or Delete Folder")
        }
        builder.setView(buildView())
        builder.setPositiveButton("Save") { _, _ ->
            val emoji = editFolderDialogEmoji.text.toString()
            val name = editFolderDialogName.text.toString()
            if (folder_id == (-1).toLong()) {
                // folder is being created
                val parentId = activity.path.last()
                val folderId = activity.databaseHelper.insertFolder(emoji, name, parentId)
                val folderItem = FolderItem(false, folderId, emoji, name, null)
                activity.folderItems.add(0, folderItem)
                activity.adapter.notifyItemInserted(0)
            } else {
                // folder is being updated
                activity.databaseHelper.updateFolder(folder_id, emoji, name)
                activity.folderItems[activity.currentItemIdx!!].emoji = emoji
                activity.folderItems[activity.currentItemIdx!!].name = name
                activity.adapter.notifyItemChanged(activity.currentItemIdx!!)
                activity.currentItemIdx = null
            }
            dismiss()
        }
        if (folder_id == (-1).toLong()) {
            builder.setNegativeButton("Cancel") { _, _ -> dismiss() }
        } else {
            builder.setNegativeButton("Delete Folder") { _, _ ->
                activity.databaseHelper.deleteFolder(folder_id)
                activity.folderItems.removeAt(activity.currentItemIdx!!)
                activity.adapter.notifyItemRemoved(activity.currentItemIdx!!)
                activity.currentItemIdx = null
                dismiss()
            }
        }
        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity.currentItemIdx = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity.currentItemIdx = null
    }

    private fun buildView(): View {
        val view = View.inflate(context, R.layout.folder_edit_dialog, null)
        editFolderDialogName = view.findViewById(R.id.editFolderDialogName)
        editFolderDialogEmoji = view.findViewById(R.id.editFolderDialogEmoji)
        editFolderRootView = view.findViewById(R.id.edit_folder_dialog_root_view)
        if (activity.currentItemIdx != null) {
            // folder is being edited
            val name = activity.folderItems[activity.currentItemIdx!!].name
            editFolderDialogName.setText(name)
        }
        setUpEmojiPopup()
        return editFolderRootView
    }

    private fun setUpEmojiPopup() {
        editFolderDialogEmoji.isCursorVisible = false
        editFolderDialogEmoji.inputType = InputType.TYPE_NULL
        editFolderDialogEmoji.setBackgroundResource(android.R.color.transparent)
        editFolderDialogEmoji.setText(if (activity.currentItemIdx != null) {
            // folder is being edited
            activity.folderItems[activity.currentItemIdx!!].emoji
        } else {
            // folder is being created
            APNotepadConstants.DEFAULT_EMOJI
        })
        val emojiPopup = EmojiPopup.Builder.fromRootView(editFolderRootView)
            .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
            .setOnEmojiBackspaceClickListener {
                editFolderDialogEmoji.setText(APNotepadConstants.DEFAULT_EMOJI)
            }
            .build(editFolderDialogEmoji)
        editFolderDialogEmoji.disableKeyboardInput(emojiPopup)
        editFolderDialogEmoji.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editFolderDialogEmoji.removeTextChangedListener(this)
                val emoji = s.subSequence(start, start + count)
                val validInput = emoji.isNotEmpty() && EmojiUtils.isOnlyEmojis(emoji) &&
                        EmojiUtils.emojisCount(emoji) == 1
                if (validInput) {
                    editFolderDialogEmoji.setText(emoji)
                } else {
                    editFolderDialogEmoji.setText(APNotepadConstants.DEFAULT_EMOJI)
                }
                editFolderDialogEmoji.addTextChangedListener(this)
            }

            override fun afterTextChanged(s: Editable) {
                editFolderDialogEmoji.setSelection(editFolderDialogEmoji.text.toString().length)
            }
        })
    }
}