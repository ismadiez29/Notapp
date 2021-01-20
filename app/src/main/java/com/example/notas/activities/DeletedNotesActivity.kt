package com.example.notas.activities

import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notas.R
import com.example.notas.adapters.DeletedNotesAdapter
import com.example.notas.adapters.NotesAdapter
import com.example.notas.database.NotesDatabase
import com.example.notas.entities.DeletedNote
import com.example.notas.entities.Note
import com.example.notas.listeners.NotesListener

class DeletedNotesActivity : AppCompatActivity(), NotesListener {

    private var deletedNoteList = mutableListOf<DeletedNote>()
    private lateinit var notesAdapter: DeletedNotesAdapter
    private lateinit var deletedNotesRecyclerView: RecyclerView

    companion object{
        const val REQUEST_CODE_SHOW_NOTES = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_deleted_notes)

        deletedNotesRecyclerView = findViewById(R.id.deletedNotesRecyclerView)
        deletedNotesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        deletedNoteList = ArrayList<DeletedNote>()
        notesAdapter = DeletedNotesAdapter(deletedNoteList as ArrayList<DeletedNote>, this)
        deletedNotesRecyclerView.adapter = notesAdapter

        getNotes(DeletedNotesActivity.REQUEST_CODE_SHOW_NOTES)

    }

    private fun getNotes(requestCode: Int) {
        class GetNoteTask : AsyncTask<Void, Void, List<DeletedNote>>() {
            override fun doInBackground(vararg params: Void?): List<DeletedNote> {
                return NotesDatabase.db.getDatabase(applicationContext).DeletedNoteDao().getAllDeletedNotes();

            }

            override fun onPostExecute(notes: List<DeletedNote>) {
                super.onPostExecute(notes)
                if (requestCode == DeletedNotesActivity.REQUEST_CODE_SHOW_NOTES) {
                    deletedNoteList.addAll(notes)
                    System.out.println("Tamaño: " + deletedNoteList.size)
                    notesAdapter.notifyDataSetChanged();
                }
            }
        }
        GetNoteTask().execute();
    }
}