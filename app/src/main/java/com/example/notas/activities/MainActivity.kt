package com.example.notas.activities

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notas.R
import com.example.notas.adapters.NotesAdapter
import com.example.notas.adapters.nA
import com.example.notas.database.NotesDatabase
import com.example.notas.entities.Note
import java.util.Collections.addAll

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var notesRecyclerView : RecyclerView
    private var notelist = mutableListOf<Note>()
    private lateinit var notesAdapter: NotesAdapter

    companion object{
        val REQUEST_CODE_ADD_NOTE = 1;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

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
        notesRecyclerView.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)

        notelist = ArrayList<Note>()
        notesAdapter = NotesAdapter(notelist as ArrayList<Note>)
        notesRecyclerView.adapter = notesAdapter

        getNotes()
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

    private fun getNotes(){
        class GetNoteTask : AsyncTask<Void, Void, List<Note>>() {
            override fun doInBackground(vararg params: Void?): List<Note> {
                return NotesDatabase.db.getDatabase(applicationContext).noteDao().getAllNotes();

            }

             override fun onPostExecute(notes: List<Note>) {
                super.onPostExecute(notes)
                 if (notelist.size == 0){
                     notelist.addAll(notes)
                     notesAdapter.notifyDataSetChanged();
                 } else {
                     notelist.add(0,notes.get(0))
                     notesAdapter.notifyItemInserted(0)
                 }
                 notesRecyclerView.smoothScrollToPosition(0)
            }
        }
        GetNoteTask().execute();
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode ==  REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes()
        }
    }
}