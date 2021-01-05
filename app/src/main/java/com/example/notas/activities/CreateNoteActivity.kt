package com.example.notas.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.notas.R
import com.example.notas.dao.NoteDao
import com.example.notas.database.NotesDatabase
import com.example.notas.entities.Note
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.lang.String.format
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var inputNoteTitle : EditText
    private lateinit var inputNoteSubTitle : EditText
    private lateinit var inputNoteText : EditText
    private lateinit var textDateTime : TextView
    lateinit var selectedColor : String
    lateinit var viewSubtitleIndicator : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        inputNoteTitle = findViewById(R.id.inputNoteTitle)
        inputNoteSubTitle = findViewById(R.id.inputNoteSubtitle)
        inputNoteText = findViewById(R.id.inputNote)
        textDateTime = findViewById(R.id.textDateTime)
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator)

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

        selectedColor = "#333333" //Default note color

        initMiscellaneous()
        setSubtitleIndicatorColor()
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
        note.setDateTime(textDateTime?.text.toString())
        note.setColor(selectedColor)

        class SaveNoteTask : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                NotesDatabase.db.getDatabase(applicationContext).noteDao().insertNote(note)
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

    fun initMiscellaneous() {
        val layoutMiscellaneous = findViewById<LinearLayout>(R.id.layoutMiscellaneous)
        val bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous)
        layoutMiscellaneous.findViewById<TextView>(R.id.textMiscellaneous).setOnClickListener{
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        val imageColor1 : ImageView = layoutMiscellaneous.findViewById(R.id.imageColor1)
        val imageColor2 : ImageView = layoutMiscellaneous.findViewById(R.id.imageColor2)
        val imageColor3 : ImageView = layoutMiscellaneous.findViewById(R.id.imageColor3)
        val imageColor4 : ImageView = layoutMiscellaneous.findViewById(R.id.imageColor4)


        layoutMiscellaneous.findViewById<View>(R.id.viewColor1).setOnClickListener{
            selectedColor = "#333333"
            imageColor1.setImageResource(R.drawable.ic_done)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor2).setOnClickListener{
            selectedColor = "#FDBE3B"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(R.drawable.ic_done)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor3).setOnClickListener{
            selectedColor = "#FF4842"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(R.drawable.ic_done)
            imageColor4.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor4).setOnClickListener{
            selectedColor = "#3A52FC"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(R.drawable.ic_done)
            setSubtitleIndicatorColor()
        }
    }

    private fun setSubtitleIndicatorColor(){
        var gradientDrawable : GradientDrawable = viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedColor))
    }
}