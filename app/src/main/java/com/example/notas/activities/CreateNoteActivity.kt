package com.example.notas.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.notas.R
import com.example.notas.dao.NoteDao
import com.example.notas.database.NotesDatabase
import com.example.notas.entities.Note
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.InputStream
import java.lang.String.format
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var inputNoteTitle : EditText
    private lateinit var inputNoteSubTitle : EditText
    private lateinit var inputNoteText : EditText
    private lateinit var textDateTime : TextView
    lateinit var imageNote :ImageView
    lateinit var selectedColor : String
    lateinit var viewSubtitleIndicator : View
    lateinit var selectedImagePath : String

    companion object{
        val REQUEST_CODE_STORAGE_PERMISSION = 1;
        val REQUEST_CODE_SELECT_IMAGE = 2;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        inputNoteTitle = findViewById(R.id.inputNoteTitle)
        inputNoteSubTitle = findViewById(R.id.inputNoteSubtitle)
        inputNoteText = findViewById(R.id.inputNote)
        textDateTime = findViewById(R.id.textDateTime)
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator)
        imageNote = findViewById(R.id.imageNote)

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
        selectedImagePath = ""

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
        note.setImagePath(selectedImagePath)

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

        layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutAddImage).setOnClickListener(){
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            if (ContextCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                Array(1){android.Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_STORAGE_PERMISSION)
            } else {
                selectImage()
            }
        }
    }

    private fun setSubtitleIndicatorColor(){
        var gradientDrawable : GradientDrawable = viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedColor))
    }

    fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //Handling the result for the selected image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                var selectedImageUri : Uri? = data.data
                if (selectedImageUri != null) {
                    try {
                        var inputStream : InputStream? = contentResolver.openInputStream(selectedImageUri)
                        var bitmap : Bitmap = BitmapFactory.decodeStream(inputStream)
                        imageNote.setImageBitmap(bitmap)
                        imageNote.visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(selectedImageUri)

                    }catch (exception : Exception){
                        Toast.makeText(this,exception.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun getPathFromUri(contentUri: Uri): String {
        var filePath : String
        var cursor : Cursor? = contentResolver
                .query(contentUri,null,null,null,null)
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
}