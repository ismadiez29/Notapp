package com.example.notas.activities

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.notas.R
import com.example.notas.database.NotesDatabase
import com.example.notas.entities.Note
import java.lang.String.format
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var inputNoteTitle : EditText
    private lateinit var inputNoteSubTitle : EditText
    private lateinit var inputNoteText : EditText
    private lateinit var textDateTime : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        inputNoteTitle = findViewById(R.id.inputNoteTitle)
        inputNoteSubTitle = findViewById(R.id.inputNoteSubtitle)
        inputNoteText = findViewById(R.id.inputNote)
        textDateTime = findViewById(R.id.textDateTime)

        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener { onBackPressed() }
        textDateTime.text = (
                SimpleDateFormat("EEEE, dd MMM yyyy HH:mm", Locale.getDefault())
                .format(Date())
        )

        var imageSave: ImageView = findViewById(R.id.imageSave)

        imageSave.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote(){

        if (inputNoteTitle?.text.toString().trim().isEmpty() &&
                inputNoteSubTitle?.text.toString().trim().isEmpty() &&
                inputNoteText?.text.toString().trim().isEmpty()){
            Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
        }
        val note = Note();
        note.setTitle(inputNoteTitle?.text.toString())
        note.setSubtitle(inputNoteSubTitle?.text.toString())
        note.setNoteText(inputNoteText?.text.toString())

        class SaveNoteTask : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                var intent : Intent
                setResult(RESULT_OK);
                finish()
            }
        }
        SaveNoteTask().execute();
    }
}