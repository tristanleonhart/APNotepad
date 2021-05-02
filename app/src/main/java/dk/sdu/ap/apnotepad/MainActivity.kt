package dk.sdu.ap.apnotepad

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextSwitcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.MenuCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cafe.adriel.krumbsview.KrumbsView
import cafe.adriel.krumbsview.model.Krumb
import com.google.gson.*
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

class MainActivity : AppCompatActivity(), FolderItemRecyclerViewAdapter.ItemClickListener {
    lateinit var databaseHelper: DatabaseHelper

    private lateinit var placeholder : ViewGroup
    private lateinit var breadcrumbs : KrumbsView

    private var openDialog : AlertDialog? = null
    private var restoreDeleteNoteDialog = false

    var path: MutableList<Long> = ArrayList()

    var currentItemIdx: Int = -1
    val folderItems : ArrayList<FolderItem> = ArrayList()
    val adapter = FolderItemRecyclerViewAdapter(folderItems)

    private val gson = Gson()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // initialize menu to create notes and folders
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.create_menu, menu)
        MenuCompat.setGroupDividerEnabled(menu, true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.add_note_plaintext || item.itemId == R.id.add_note_checklist) {
            // create a note (plaintext or checklist)
            val intent = Intent(applicationContext, NoteEditorActivity::class.java)
            val type = if (item.itemId == R.id.add_note_plaintext) {
                APNotepadConstants.NOTE_TYPE_PLAINTEXT
            } else {
                APNotepadConstants.NOTE_TYPE_CHECKLIST
            }
            intent.putExtra("type", type)
            intent.putExtra("folderId", path.last())
            // start the note editor activity
            startActivityForResult(intent, APNotepadConstants.REQUEST_CODE_EDIT_NOTE)
            return true
        } else if (item.itemId == R.id.add_folder) {
            // create a folder or subfolder
            FolderEditDialog().show(supportFragmentManager, null)
            return true
        } else if (item.itemId == R.id.import_notes) {
            // import notes and folders from a json file
            importData()
            return true
        } else if (item.itemId == R.id.export_notes) {
            // export notes and folders to a json file
            exportData()
            return true
        } else if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enable the emoji layout components
        EmojiManager.install(IosEmojiProvider())
        setContentView(R.layout.activity_main)

        // get placeholder for empty RecyclerView
        placeholder = findViewById(R.id.folderItemsPlaceholder)

        // configure breadcrumbs view
        breadcrumbs = findViewById(R.id.breadcrumbs)
        breadcrumbs.setOnPreviousItemClickListener {
            // go up one folder level
            folderLevelUp(false)
        }
        // fix breadcrumbs previous item width bug
        val breadcrumbsPrevItem = findViewById<TextSwitcher>(R.id.vBreadcrumbPreviousItemSwitcher)
        breadcrumbsPrevItem.measureAllChildren = false
        breadcrumbsPrevItem.minimumWidth = 0

        // uncomment line below to reset database on app startup
        //applicationContext.deleteDatabase(DatabaseHelper.DB_NAME)

        // get database
        databaseHelper = DatabaseHelper.getInstance(this)

        // check if database exists
        if (!applicationContext.databaseList().contains(DatabaseHelper.DB_NAME)) {
            loadDefaultData()
        }

        // check if a saved path is available
        val savedPath = savedInstanceState?.getLongArray("path")
        if (savedPath != null) {
            // restore the path to the previous folder
            path = savedPath.toMutableList()
        } else {
            // set the path to the root folder
            path.clear()
            path.add(APNotepadConstants.ROOT_FOLDER_ID)
        }

        // display a back arrow if we are in a sub folder
        updateBackArrow()

        // restore the current item index
        currentItemIdx = savedInstanceState?.getInt("currentItemIdx", -1) ?: -1

        // restore open dialogs
        if (savedInstanceState?.getBoolean("restoreDeleteNoteDialog", false) == true) {
            showDeleteNoteDialog()
        }

        // load the items inside the current folder
        folderItems.clear()
        folderItems.addAll(databaseHelper.getFolderItems(path.last()))
        adapter.notifyDataSetChanged()

        // listen for item clicks
        adapter.setItemClickListener(this)
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
        val recyclerView = findViewById<RecyclerView>(R.id.folderItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            APNotepadConstants.REQUEST_CODE_EDIT_NOTE -> {
                val intent = data as Intent
                if (intent.getBooleanExtra("noteCreated", false)) {
                    // a note was created
                    val noteId = intent.getLongExtra("noteId", -1)
                    val note = databaseHelper.getNote(noteId)
                    val folderItem = NoteUtils.folderItemFromNote(note)
                    // check if the folder item is already present
                    var index = folderItems.indexOf(folderItem)
                    if (index >= 0) {
                        // update the folder item
                        folderItems[index] = folderItem
                        adapter.notifyItemChanged(index)
                    } else {
                        // insert the folder item
                        index = getInsertNoteIndex()
                        folderItems.add(index, folderItem)
                        adapter.notifyItemInserted(index)
                    }
                } else {
                    // a note was updated
                    val noteId = folderItems[currentItemIdx].id
                    val note = databaseHelper.getNote(noteId)
                    folderItems[currentItemIdx] = NoteUtils.folderItemFromNote(note)
                    adapter.notifyItemChanged(currentItemIdx)
                    currentItemIdx = -1
                }
            }
            APNotepadConstants.REQUEST_CODE_IMPORT_NOTES -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    try {
                        val uri = data.data!!
                        val root : JsonObject
                        contentResolver.openInputStream(uri).use { inputStream ->
                            InputStreamReader(inputStream).use {
                                // parse json file
                                root = JsonParser.parseReader(it).asJsonObject
                            }
                        }
                        // check file format
                        if (root.get("application").asString != "APNotepad") {
                            importFailed()
                            return
                        }
                        // reset the database
                        databaseHelper.close()
                        applicationContext.deleteDatabase(DatabaseHelper.DB_NAME)
                        // read data from json
                        readFolderContents(root, APNotepadConstants.ROOT_FOLDER_ID)
                        // go to the root folder
                        toRootFolder()
                        // import succeeded
                        Toast.makeText(this, "Import successful!", Toast.LENGTH_SHORT).show()
                    } catch (e : Exception) {
                        importFailed()
                    }
                } else {
                    importFailed()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (path.last() != APNotepadConstants.ROOT_FOLDER_ID) {
            // go up one folder level
            folderLevelUp(true)
        } else {
            // we are at the root folder level (normal back press behaviour)
            super.onBackPressed()
        }
    }

    private fun folderLevelUp(updateBreadcrumbs: Boolean) {
        path.removeLast()
        updateBackArrow()
        if (updateBreadcrumbs) {
            breadcrumbs.removeLastItem()
        }
        folderItems.clear()
        folderItems.addAll(databaseHelper.getFolderItems(path.last()))
        adapter.notifyDataSetChanged()
    }

    private fun toRootFolder() {
        // go to the root folder
        path.clear()
        path.add(APNotepadConstants.ROOT_FOLDER_ID)
        updateBackArrow()
        breadcrumbs.goToFirstItem()
        folderItems.clear()
        folderItems.addAll(databaseHelper.getFolderItems(path.last()))
        adapter.notifyDataSetChanged()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLongArray("path", path.toLongArray())
        outState.putInt("currentItemIdx", currentItemIdx)
        outState.putBoolean("restoreDeleteNoteDialog", restoreDeleteNoteDialog)
    }

    override fun onDestroy() {
        super.onDestroy()
        openDialog?.dismiss()
    }

    override fun onItemClick(position: Int) {
        if (folderItems[position].isNote) {
            // a note was clicked (open it)
            currentItemIdx = position
            val note = databaseHelper.getNote(folderItems[position].id)
            val intent = Intent(applicationContext, NoteEditorActivity::class.java)
            intent.putExtra("noteId", note.id)
            intent.putExtra("folderId", path.last())
            // start the note editor activity
            startActivityForResult(intent, APNotepadConstants.REQUEST_CODE_EDIT_NOTE)
        } else {
            // a folder was clicked (show its contents)
            val folderId = folderItems[position].id
            // go down one folder level
            path.add(folderId)
            updateBackArrow()
            val folder = databaseHelper.getFolder(folderId)
            val breadcrumbText = if (folder.name.isEmpty()) {
                APNotepadConstants.UNTITLED_PLACEHOLDER
            } else {
                folder.name
            }
            breadcrumbs.addItem(Krumb(breadcrumbText))
            folderItems.clear()
            folderItems.addAll(databaseHelper.getFolderItems(path.last()))
            adapter.notifyDataSetChanged()
        }
    }

    override fun onItemLongClick(position: Int) {
        // save current item index
        currentItemIdx = position
        if (folderItems[position].isNote) {
            // show delete note dialog
            showDeleteNoteDialog()
        } else {
            // show edit or delete folder dialog
            FolderEditDialog().show(supportFragmentManager, null)
        }
    }

    private fun showDeleteNoteDialog() {
        restoreDeleteNoteDialog = true
        openDialog = AlertDialog.Builder(this@MainActivity)
            .setTitle("Delete Note")
            .setMessage("Do you want to delete this note?")
            .setPositiveButton("Yes") { _, _ ->
                databaseHelper.deleteNote(folderItems[currentItemIdx].id)
                folderItems.removeAt(currentItemIdx)
                adapter.notifyItemRemoved(currentItemIdx)
            }
            .setNegativeButton("No", null)
            .setOnDismissListener {
                currentItemIdx = -1
                restoreDeleteNoteDialog = false
                openDialog = null
            }
            .show()
    }

    private fun getInsertNoteIndex(): Int {
        var index = 0
        for (folderItem in folderItems) {
            if (folderItem.isNote) {
                break
            }
            index++
        }
        return index
    }

    private fun updateBackArrow() {
        if (path.size > 1) {
            // we are in a sub folder
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun importData() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    APNotepadConstants.REQUEST_CODE_PERMISSIONS_IMPORT
                )
                return
            }
        }
        browseFiles()
    }

    private fun browseFiles() {
        val browse = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }, null)
        startActivityForResult(browse, APNotepadConstants.REQUEST_CODE_IMPORT_NOTES)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            APNotepadConstants.REQUEST_CODE_PERMISSIONS_IMPORT -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    browseFiles()
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun importFailed() {
        Toast.makeText(this, "Import failed!", Toast.LENGTH_SHORT).show()
    }

    private fun exportData() {
        // create the export
        val root = JsonObject()
        root.addProperty("application", "APNotepad")
        writeFolderContents(root, APNotepadConstants.ROOT_FOLDER_ID)
        // create the directory
        val exportPath = File(filesDir, "json_export")
        if (!exportPath.exists()) {
            exportPath.mkdir()
        }
        // create the file
        val jsonFile = File(exportPath.path, "export.apn")
        FileWriter(jsonFile).use {
            gson.toJson(root, it)
        }
        // share the file
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", jsonFile)
        val share = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            clipData = ClipData.newRawUri("", uri)
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "text/json"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, null)
        startActivity(share)
    }

    private fun loadDefaultData() {
        val root : JsonObject
        resources.openRawResource(R.raw.default_data).use { inputStream ->
            InputStreamReader(inputStream).use {
                // parse json file
                root = JsonParser.parseReader(it).asJsonObject
            }
        }
        // check file format
        if (root.get("application").asString == "APNotepad") {
            // read data from json
            readFolderContents(root, APNotepadConstants.ROOT_FOLDER_ID)
        }
    }

    private fun readFolderContents(source: JsonObject, folder_id: Long) {
        val notes = source.get("notes").asJsonArray
        val folders = source.get("folders").asJsonArray
        for (noteJson in notes) {
            val note = gson.fromJson(noteJson, Note::class.java)
            databaseHelper.insertNote(note, folder_id)
        }
        for (folderJson in folders) {
            val folder = gson.fromJson(folderJson, Folder::class.java)
            databaseHelper.insertFolder(folder)
            readFolderContents(folderJson.asJsonObject, folder.id)
        }
    }

    private fun writeFolderContents(target: JsonObject, folder_id: Long) {
        // get the folder contents
        val items = databaseHelper.getFolderItems(folder_id)
        val notes = JsonArray()
        val folders = JsonArray()
        for (item in items) {
            if (item.isNote) {
                val note = databaseHelper.getNote(item.id)
                val noteJson = gson.toJsonTree(note)
                notes.add(noteJson)
            } else {
                val folder = databaseHelper.getFolder(item.id)
                val folderJson = gson.toJsonTree(folder)
                writeFolderContents(folderJson.asJsonObject, item.id)
                folders.add(folderJson)
            }
        }
        target.add("notes", notes)
        target.add("folders", folders)
    }
}