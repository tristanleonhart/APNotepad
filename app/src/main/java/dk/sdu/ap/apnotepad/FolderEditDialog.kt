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

class FolderEditDialog : DialogFragment() {
    private lateinit var activity : MainActivity

    private lateinit var editFolderRootView : ViewGroup
    private lateinit var editFolderDialogEmoji : EmojiEditText
    private lateinit var editFolderDialogName : EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity = requireActivity() as MainActivity
        val builder = AlertDialog.Builder(requireContext())
        if (activity.currentItemIdx >= 0) {
            builder.setTitle("Edit or Delete Folder")
        } else {
            builder.setTitle("Create Folder")
        }
        builder.setView(buildView())
        builder.setPositiveButton("Save") { _, _ ->
            val emoji = editFolderDialogEmoji.text.toString()
            val name = editFolderDialogName.text.toString()
            if (activity.currentItemIdx >= 0) {
                // folder is being updated
                val folderId = activity.folderItems[activity.currentItemIdx].id
                val folder = Folder(folderId, emoji, name, activity.path.last())
                activity.databaseHelper.updateFolder(folder)
                activity.folderItems[activity.currentItemIdx].emoji = emoji
                activity.folderItems[activity.currentItemIdx].name = name
                activity.adapter.notifyItemChanged(activity.currentItemIdx)
            } else {
                // folder is being created
                val folder = Folder(
                    APNotepadConstants.AUTO_INCREMENT_ID,
                    emoji,
                    name,
                    activity.path.last()
                )
                val folderId = activity.databaseHelper.insertFolder(folder)
                val folderItem = FolderItem(false, folderId, emoji, name, null)
                activity.folderItems.add(0, folderItem)
                activity.adapter.notifyItemInserted(0)
            }
        }
        if (activity.currentItemIdx >= 0) {
            builder.setNegativeButton("Delete Folder") { _, _ ->
                val folderId = activity.folderItems[activity.currentItemIdx].id
                activity.databaseHelper.deleteFolder(folderId)
                activity.folderItems.removeAt(activity.currentItemIdx)
                activity.adapter.notifyItemRemoved(activity.currentItemIdx)
            }
        } else {
            builder.setNegativeButton("Cancel", null)
        }
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity.currentItemIdx = -1
    }

    private fun buildView(): View {
        val view = View.inflate(context, R.layout.folder_edit_dialog, null)
        editFolderDialogName = view.findViewById(R.id.editFolderDialogName)
        editFolderDialogEmoji = view.findViewById(R.id.editFolderDialogEmoji)
        editFolderRootView = view.findViewById(R.id.editFolderDialogRootView)
        if (activity.currentItemIdx >= 0) {
            // folder is being edited
            val name = activity.folderItems[activity.currentItemIdx].name
            editFolderDialogName.setText(name)
        }
        setUpEmojiPopup()
        return editFolderRootView
    }

    private fun setUpEmojiPopup() {
        editFolderDialogEmoji.isCursorVisible = false
        editFolderDialogEmoji.inputType = InputType.TYPE_NULL
        editFolderDialogEmoji.setBackgroundResource(android.R.color.transparent)
        editFolderDialogEmoji.setText(if (activity.currentItemIdx >= 0) {
            // folder is being edited
            activity.folderItems[activity.currentItemIdx].emoji
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