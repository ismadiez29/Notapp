package com.example.notas.ui.main

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
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notas.R
import com.example.notas.activities.CreateNoteActivity
import com.example.notas.activities.MainActivity
import com.example.notas.adapters.NotesAdapter
import com.example.notas.database.NotesDatabase
import com.example.notas.entities.Note
import com.example.notas.listeners.NotesListener
import com.example.notas.ui.deletedNotes.DeletedNotesFragment
import com.google.android.material.navigation.NavigationView

class MainFragment: Fragment(), NotesListener {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val x = inflater.inflate(R.layout.fragment_main, container, false)

        var imageAddNoteMain: ImageView = x.findViewById(R.id.imageAddNoteMain)

        imageAddNoteMain.setOnClickListener {
            val intent = Intent(activity, CreateNoteActivity::class.java)
            startActivityForResult(intent, MainActivity.REQUEST_CODE_ADD_NOTE)
        }
        notesRecyclerView = x.findViewById(R.id.notesRecyclerView)
        notesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        notelist = ArrayList<Note>()
        notesAdapter = NotesAdapter(notelist as ArrayList<Note>, this)
        notesRecyclerView.adapter = notesAdapter

        getNotes(MainActivity.REQUEST_CODE_SHOW_NOTES, false)

        inputSearch = x.findViewById(R.id.inputSearch)
        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                notesAdapter.cancelTimer()
            }

            override fun afterTextChanged(s: Editable?) {
                if (notelist.size != 0) {
                    notesAdapter.searchNotes(s.toString())
                }
            }
        })
        x.findViewById<ImageView>(R.id.imageAddNote).setOnClickListener(){
            val intent = Intent(activity, CreateNoteActivity::class.java)
            startActivityForResult(intent, MainActivity.REQUEST_CODE_ADD_NOTE)
        }

        x.findViewById<ImageView>(R.id.imageAddImage).setOnClickListener(){
            if (ContextCompat.checkSelfPermission(
                            requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        Array(1) { android.Manifest.permission.READ_EXTERNAL_STORAGE },
                        MainActivity.REQUEST_CODE_STORAGE_PERMISSION)
            } else {
                selectImage()
            }
        }

        x.findViewById<ImageView>(R.id.imageAddWebLink).setOnClickListener(){
            showAddURLDialog()
        }

        return x
    }

    private fun selectImage() {

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (context?.let { intent.resolveActivity(it.packageManager) } != null) {
            startActivityForResult(intent, MainActivity.REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CreateNoteActivity.REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                Toast.makeText(activity, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getPathFromUri(contentUri: Uri): String {
        var filePath: String
        var cursor: Cursor? = context?.contentResolver
                ?.query(contentUri, null, null, null, null)
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

    override fun onNoteClicked(note: Note, position: Int){
        noteClickedPosition = position
        val intent = Intent(context, CreateNoteActivity::class.java)
        intent.putExtra("isViewOrUpdate", true)
        intent.putExtra("note", note)
        startActivityForResult(intent, MainActivity.REQUEST_CODE_UPDATE_NOTE)

    }

    private fun getNotes(requestCode: Int, isNoteDelete: Boolean) {
        class GetNoteTask : AsyncTask<Void, Void, List<Note>>() {
            override fun doInBackground(vararg params: Void?): List<Note> {
                return NotesDatabase.db.getDatabase(context!!).noteDao().getAllNotes();

            }

            override fun onPostExecute(notes: List<Note>) {
                super.onPostExecute(notes)
                if (requestCode == MainActivity.REQUEST_CODE_SHOW_NOTES) {
                    notelist.addAll(notes)
                    notesAdapter.notifyDataSetChanged();
                } else if (requestCode == MainActivity.REQUEST_CODE_ADD_NOTE){
                    notelist.add(0, notes[0])
                    notesAdapter.notifyItemInserted(0)
                    notesRecyclerView.smoothScrollToPosition(0)
                }else if (requestCode == MainActivity.REQUEST_CODE_UPDATE_NOTE){
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
        if (requestCode == MainActivity.REQUEST_CODE_ADD_NOTE && resultCode == AppCompatActivity.RESULT_OK) {
            getNotes(MainActivity.REQUEST_CODE_ADD_NOTE, false)
        }else if( requestCode == MainActivity.REQUEST_CODE_UPDATE_NOTE && resultCode == AppCompatActivity.RESULT_OK){
            if(data != null){
                getNotes(MainActivity.REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false))
            }
        }else if(requestCode == MainActivity.REQUEST_CODE_SELECT_IMAGE && resultCode == AppCompatActivity.RESULT_OK){
            if(data != null){
                var selectedImageUri: Uri? = data.getData()
                if(selectedImageUri != null){
                    try{
                        var selectedImagePath = getPathFromUri(selectedImageUri)
                        val intent: Intent = Intent(context, CreateNoteActivity::class.java)
                        intent.putExtra("isFromQuickAction", true)
                        intent.putExtra("quickActionType", "image")
                        intent.putExtra("imagePath", selectedImagePath)
                        startActivityForResult(intent, MainActivity.REQUEST_CODE_ADD_NOTE)
                    }catch (ex: Exception){
                        Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun showAddURLDialog(){
        //if (dialogAddURL == null){
        var builder : AlertDialog.Builder = AlertDialog.Builder(requireContext())
        var view : View = LayoutInflater.from(context).inflate(
                R.layout.layout_add_url,
                view?.findViewById<ViewGroup>(R.id.layoutAddUrlContainer)
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
                Toast.makeText(activity, "Enter URL", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.WEB_URL.matcher(inputURL.text.toString()).matches()){
                Toast.makeText(activity, "Enter valid URL", Toast.LENGTH_SHORT).show()
            } else {
                dialogAddURL.dismiss()
                val intent: Intent = Intent(context, CreateNoteActivity::class.java)
                intent.putExtra("isFromQuickAction", true)
                intent.putExtra("quickActionType", "URL")
                intent.putExtra("URL", inputURL.text.toString())
                startActivityForResult(intent, MainActivity.REQUEST_CODE_ADD_NOTE)
            }
        }
        view.findViewById<TextView>(R.id.textCancel).setOnClickListener(){
            dialogAddURL.dismiss()
        }
        //}
        dialogAddURL.show()
    }
}