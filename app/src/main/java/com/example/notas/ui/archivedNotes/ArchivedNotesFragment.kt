package com.example.notas.ui.archivedNotes

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notas.R
import com.example.notas.adapters.ArchivedNotesAdapter
import com.example.notas.database.NotesDatabase
import com.example.notas.entities.ArchivedNote
import com.example.notas.entities.DeletedNote
import com.example.notas.listeners.NotesListener

class ArchivedNotesFragment: Fragment(), NotesListener {

    private var archivedNoteList = mutableListOf<ArchivedNote>()
    private lateinit var notesAdapter: ArchivedNotesAdapter
    private lateinit var ArchivedNotesRecyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    companion object{
        const val REQUEST_CODE_SHOW_NOTES = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getNotes(REQUEST_CODE_SHOW_NOTES)
    }

    private fun getNotes(requestCode: Int) {
        class GetNoteTask : AsyncTask<Void, Void, List<ArchivedNote>>() {
            override fun doInBackground(vararg params: Void?): List<ArchivedNote> {
                return NotesDatabase.db.getDatabase(activity!!.baseContext).ArchivedNoteDao().getAllArchivedNotes();

            }
            override fun onPostExecute(notes: List<ArchivedNote>) {
                super.onPostExecute(notes)
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    archivedNoteList.addAll(notes)
                    System.out.println("Tamaño: " + archivedNoteList.size)
                    notesAdapter.notifyDataSetChanged();
                }
            }
        }
        GetNoteTask().execute();
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val x = inflater.inflate(R.layout.fragment_archived_notes, container, false)

        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        ArchivedNotesRecyclerView = x.findViewById(R.id.archivedNotesRecyclerView)!!
        ArchivedNotesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        archivedNoteList = ArrayList<ArchivedNote>()
        notesAdapter = ArchivedNotesAdapter(archivedNoteList as ArrayList<ArchivedNote>, this)
        ArchivedNotesRecyclerView.adapter = notesAdapter

        return x
    }

}