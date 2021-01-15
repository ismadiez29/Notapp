package com.example.notas.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notas.R
import com.example.notas.adapters.NotesAdapter
import com.example.notas.database.NotesDatabase
import com.example.notas.entities.Note
import com.example.notas.listeners.NotesListener
import java.lang.Exception

class MainActivity : AppCompatActivity(), NotesListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var notesRecyclerView: RecyclerView
    private var notelist = mutableListOf<Note>()
    private lateinit var notesAdapter: NotesAdapter
    private var noteClickedPosition = -1
    private lateinit var inputSearch : EditText
    private lateinit var dialogAddURL: AlertDialog

    companion object {
        const val REQUEST_CODE_ADD_NOTE = 1;
        const val REQUEST_CODE_UPDATE_NOTE = 2
        const val REQUEST_CODE_SHOW_NOTES = 3
        const val REQUEST_CODE_SELECT_IMAGE = 4
        const val REQUEST_CODE_STORAGE_PERMISSION = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        supportActionBar?.title = "My Notes"
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Notes"
        supportActionBar!!.title = "My Notes"

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        var imageAddNoteMain: ImageView = findViewById(R.id.imageAddNoteMain)

        imageAddNoteMain.setOnClickListener {
            val intent = Intent(this, CreateNoteActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
        }
        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        notesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        notelist = ArrayList<Note>()
        notesAdapter = NotesAdapter(notelist as ArrayList<Note>, this)
        notesRecyclerView.adapter = notesAdapter

        getNotes(REQUEST_CODE_SHOW_NOTES,false)

        inputSearch = findViewById(R.id.inputSearch)
        inputSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                notesAdapter.cancelTimer()
            }

            override fun afterTextChanged(s: Editable?) {
                if(notelist.size != 0){
                    notesAdapter.searchNotes(s.toString())
                }
            }
        })
        findViewById<ImageView>(R.id.imageAddNote).setOnClickListener(){
            val intent = Intent(this, CreateNoteActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
        }

        findViewById<ImageView>(R.id.imageAddImage).setOnClickListener(){
            if (ContextCompat.checkSelfPermission(
                            applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        Array(1) { android.Manifest.permission.READ_EXTERNAL_STORAGE },
                        REQUEST_CODE_STORAGE_PERMISSION)
            } else {
                selectImage()
            }
        }

        findViewById<ImageView>(R.id.imageAddWebLink).setOnClickListener(){
            showAddURLDialog()
        }

    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CreateNoteActivity.REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getPathFromUri(contentUri: Uri): String {
        var filePath: String
        var cursor: Cursor? = contentResolver
                .query(contentUri, null, null, null, null)
        if (cursor == null) {
            filePath = contentUri.path.toString()
        } else {
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNoteClicked(note: Note, position: Int){
        noteClickedPosition = position
        val intent = Intent(this, CreateNoteActivity::class.java)
        intent.putExtra("isViewOrUpdate", true)
        intent.putExtra("note",note)
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE)

    }

    private fun getNotes(requestCode: Int, isNoteDelete: Boolean) {
        class GetNoteTask : AsyncTask<Void, Void, List<Note>>() {
            override fun doInBackground(vararg params: Void?): List<Note> {
                return NotesDatabase.db.getDatabase(applicationContext).noteDao().getAllNotes();

            }

            override fun onPostExecute(notes: List<Note>) {
                super.onPostExecute(notes)
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    notelist.addAll(notes)
                    notesAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE){
                    notelist.add(0, notes[0])
                    notesAdapter.notifyItemInserted(0)
                    notesRecyclerView.smoothScrollToPosition(0)
                }else if (requestCode == REQUEST_CODE_UPDATE_NOTE){
                    System.out.println("pasa por el update")
                        notelist.removeAt(noteClickedPosition)
                    if (isNoteDelete){
                        notesAdapter.notifyItemRemoved(noteClickedPosition)
                    }else{
                        notelist.add(noteClickedPosition, notes.get(noteClickedPosition))
                        notesAdapter.notifyItemChanged(noteClickedPosition)
                    }
                }
            }
        }
        GetNoteTask().execute();
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false)
        }else if( requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            if(data != null){
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false))
            }
        }else if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                var selectedImageUri: Uri? = data.getData()
                if(selectedImageUri != null){
                    try{
                        var selectedImagePath = getPathFromUri(selectedImageUri)
                        val intent: Intent = Intent(applicationContext, CreateNoteActivity::class.java)
                        intent.putExtra("isFromQuickAction", true)
                        intent.putExtra("quickActionType", "image")
                        intent.putExtra("imagePath", selectedImagePath)
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
                    }catch (ex : Exception){
                        Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun showAddURLDialog(){
        //if (dialogAddURL == null){
        var builder : AlertDialog.Builder = AlertDialog.Builder(this)
        var view : View = LayoutInflater.from(this).inflate(
                R.layout.layout_add_url,
                findViewById<ViewGroup>(R.id.layoutAddUrlContainer)
        )
        builder.setView(view)
        dialogAddURL = builder.create()
        if (dialogAddURL.window != null){
            dialogAddURL.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        val inputURL : EditText = view.findViewById(R.id.inputUrl)
        inputURL.requestFocus()

        view.findViewById<TextView>(R.id.textAdd).setOnClickListener(){
            if (inputURL.text.toString().trim().isEmpty()){
                Toast.makeText(this,"Enter URL",Toast.LENGTH_SHORT).show()
            } else if (!Patterns.WEB_URL.matcher(inputURL.text.toString()).matches()){
                Toast.makeText(this, "Enter valid URL", Toast.LENGTH_SHORT).show()
            } else {
                dialogAddURL.dismiss()
                val intent: Intent = Intent(applicationContext, CreateNoteActivity::class.java)
                intent.putExtra("isFromQuickAction", true)
                intent.putExtra("quickActionType", "URL")
                intent.putExtra("URL", inputURL.text.toString())
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
            }
        }
        view.findViewById<TextView>(R.id.textCancel).setOnClickListener(){
            dialogAddURL.dismiss()
        }
        //}
        dialogAddURL.show()
    }

}