package com.example.notas.ui.deletedNotes

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notas.R
import com.example.notas.adapters.DeletedNotesAdapter
import com.example.notas.database.NotesDatabase
import com.example.notas.entities.DeletedNote
import com.example.notas.listeners.NotesListener

class DeletedNotesFragment: Fragment(), NotesListener {

    private var deletedNoteList = mutableListOf<DeletedNote>()
    private lateinit var notesAdapter: DeletedNotesAdapter
    private lateinit var deletedNotesRecyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    companion object{
        const val REQUEST_CODE_SHOW_NOTES = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        getNotes(REQUEST_CODE_SHOW_NOTES)
    }

    private fun getNotes(requestCode: Int) {
        class GetNoteTask : AsyncTask<Void, Void, List<DeletedNote>>() {
            override fun doInBackground(vararg params: Void?): List<DeletedNote> {

                return NotesDatabase.db.getDatabase(activity!!.baseContext).DeletedNoteDao().getAllDeletedNotes();

            }
            override fun onPostExecute(notes: List<DeletedNote>) {
                super.onPostExecute(notes)
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    deletedNoteList.addAll(notes)
                    System.out.println("Tamaño: " + deletedNoteList.size)
                    notesAdapter.notifyDataSetChanged();
                }
            }
        }
        GetNoteTask().execute();
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val x = inflater.inflate(R.layout.fragment_deleted_notes, container, false)

        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        deletedNotesRecyclerView = x.findViewById(R.id.deletedNotesRecyclerView)!!
        deletedNotesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        deletedNoteList = ArrayList<DeletedNote>()
        notesAdapter = DeletedNotesAdapter(deletedNoteList as ArrayList<DeletedNote>, this)
        deletedNotesRecyclerView.adapter = notesAdapter

        return x
    }
}