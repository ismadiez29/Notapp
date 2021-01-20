package com.example.notas.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.AsyncTask.execute
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable

import com.example.notas.R
import com.example.notas.database.NotesDatabase
import com.example.notas.database.NotesDatabase.db.getDatabase
import com.example.notas.entities.DeletedNote
import com.example.notas.entities.Note
import com.google.android.material.bottomsheet.BottomSheetBehavior
import top.defaults.colorpicker.ColorPickerPopup
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var inputNoteTitle: EditText
    private lateinit var inputNoteSubTitle: EditText
    private lateinit var inputNoteText: EditText
    private lateinit var textDateTime: TextView
    lateinit var imageNote: ImageView
    lateinit var selectedColor: String
    lateinit var viewSubtitleIndicator: View
    lateinit var selectedImagePath: String
    lateinit var textWebURL: TextView
    lateinit var layoutWebURL: LinearLayout

    lateinit var dialogAddURL: AlertDialog
    private lateinit var dialogDeleteNote: AlertDialog

    private lateinit var alreadyAvailableNote: Note

    companion object {
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
        textWebURL = findViewById(R.id.textWebURL)
        layoutWebURL = findViewById(R.id.layoutWebURL)

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
        if (intent.getBooleanExtra("isViewOrUpdate", false)){
            alreadyAvailableNote = intent.getSerializableExtra("note") as Note
            setViewOrUpdateNote()

        }else{
            alreadyAvailableNote = Note()
        }

        findViewById<ImageView>(R.id.imageRemoveWebURL).setOnClickListener(){
            textWebURL.setText(null)
            layoutWebURL.visibility = View.GONE
        }

        findViewById<ImageView>(R.id.imageRemoveImage).setOnClickListener(){
            imageNote.setImageBitmap(null)
            imageNote.visibility = View.GONE
            findViewById<ImageView>(R.id.imageRemoveImage).visibility = View.GONE
            selectedImagePath = ""
        }

        if (intent.getBooleanExtra("isFromQuickAction", false)){
            var type: String? = intent.getStringExtra("quickActionType")
            if(type != null){
                if (type.equals("image")){
                    selectedImagePath = intent.getStringExtra("imagePath").toString()
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath))
                    imageNote.visibility = View.VISIBLE
                    findViewById<ImageView>(R.id.imageRemoveImage).visibility = View.VISIBLE
                } else if (type.equals("URL")){
                    textWebURL.text = intent.getStringExtra("URL")
                    layoutWebURL.visibility = View.VISIBLE
                }
            }
        }

        initMiscellaneous()
        setSubtitleIndicatorColor()
    }

    fun setViewOrUpdateNote(){
        inputNoteTitle.setText(alreadyAvailableNote.getTitle())
        inputNoteSubTitle.setText(alreadyAvailableNote.getSubtitle())
        inputNoteText.setText(alreadyAvailableNote.getNoteText())
        textDateTime.setText(alreadyAvailableNote.getDateTime())

        if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath()!!.trim().isEmpty()){
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()))
            imageNote.visibility = View.VISIBLE
            findViewById<ImageView>(R.id.imageRemoveImage).visibility = View.VISIBLE
            selectedImagePath = alreadyAvailableNote.getImagePath()!!
        }

        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink()!!.trim().isEmpty()){
            layoutWebURL.visibility = View.VISIBLE
            textWebURL.text = alreadyAvailableNote.getWebLink()
        }

    }

    private fun saveNote() {

        if (inputNoteTitle.text.toString().trim().isEmpty() &&
                inputNoteSubTitle.text.toString().trim().isEmpty() &&
                inputNoteText.text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
        }
        val note = Note();
        note.setTitle(inputNoteTitle.text.toString())
        note.setSubtitle(inputNoteSubTitle.text.toString())
        note.setNoteText(inputNoteText.text.toString())
        note.setDateTime(textDateTime.text.toString())
        note.setColor(selectedColor)
        note.setImagePath(selectedImagePath)

        if (layoutWebURL.visibility == View.VISIBLE){
            note.setWebLink(textWebURL.text.toString())
        }

        //OnConflictStrategy is REPLACE meaning if new note has the same id of other in the db it will be replaced (updated)
        if (alreadyAvailableNote != null){
            note.setId(alreadyAvailableNote.getId())

            class UpdateNoteTask : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg params: Void?): Void? {
                    return null
                }

                override fun onPostExecute(result: Void?) {
                    super.onPostExecute(result)
                    var intent: Intent = Intent()
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
            UpdateNoteTask().execute()
        }

        class SaveNoteTask : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                NotesDatabase.db.getDatabase(applicationContext).noteDao().insertNote(note)
                return null;
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                var intent: Intent
                setResult(RESULT_OK);
                finish()
            }
        }
        SaveNoteTask().execute();
    }

    fun initMiscellaneous() {
        val layoutMiscellaneous = findViewById<LinearLayout>(R.id.layoutMiscellaneous)
        val bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous)
        layoutMiscellaneous.findViewById<TextView>(R.id.textMiscellaneous).setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        val imageColor1: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor1)
        val imageColor2: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor2)
        val imageColor3: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor3)
        val imageColor4: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor4)
        val imageColor5: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor5)
        val imageColor6: ImageView = layoutMiscellaneous.findViewById(R.id.imageColor6)


        layoutMiscellaneous.findViewById<View>(R.id.viewColor1).setOnClickListener {
            selectedColor = "#333333"
            imageColor1.setImageResource(R.drawable.ic_done)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor2).setOnClickListener {
            selectedColor = "#FDBE3B"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(R.drawable.ic_done)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(0)
            setSubtitleIndicatorColor()

        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor3).setOnClickListener {
            selectedColor = "#FF4842"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(R.drawable.ic_done)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor4).setOnClickListener {
            selectedColor = "#3A52FC"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(R.drawable.ic_done)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor5).setOnClickListener {
            selectedColor = "#884EA0"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(R.drawable.ic_done)
            imageColor6.setImageResource(0)
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<View>(R.id.viewColor6).setOnClickListener {
            selectedColor = "#17A589"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            imageColor6.setImageResource(R.drawable.ic_done)
            setSubtitleIndicatorColor()
        }

        if (alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor()!!.trim().isEmpty()){
            when(alreadyAvailableNote.getColor()){

                "#333333" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor1).performClick()
                "#FDBE3B" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor2).performClick()
                "#FF4842" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor3).performClick()
                "#3A52FC" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor4).performClick()
                "#884EA0" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor5).performClick()
                "#17A589" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor6).performClick()
            }
            setSubtitleIndicatorColor()
        }

        layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutAddImage).setOnClickListener() {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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

        layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutAddUrl).setOnClickListener(){
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showAddURLDialog()
        }
        //If is not null then the user is viewing or updating and we can display the delete button
        if(alreadyAvailableNote != null){
            layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutDeleteNote).visibility = View.VISIBLE
            layoutMiscellaneous.findViewById<LinearLayout>(R.id.layoutDeleteNote).setOnClickListener(){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                showDeleteNoteDialog()
            }
        }

        var observer: ColorPickerPopup.ColorPickerObserver
        var mDefaultColor = 0;
        layoutMiscellaneous.findViewById<TextView>(R.id.textColorPicker).setOnClickListener(){ v ->
            ColorPickerPopup.Builder(this).initialColor(Color.RED) // set initial color of the color  picker dialog
                .enableBrightness(true) // enable color brightness slider or not
                .enableAlpha(true) // enable color alpha changer on slider or not
                .okTitle("Choose") // this is top right choose button
                .cancelTitle("Cancel") // this is top left Cancel button which closes the
                .showIndicator(
                    true) // this is the small box which shows the chosen color by user at the
                // bottom of the cancel button
                .showValue(true) // this is the value which shows the selected color hex code
                // the above all values can be made false to disable them on the color picker dialog.
                .build()
                .show(v, object : ColorPickerPopup.ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        mDefaultColor = color
                        selectedColor = Integer.toHexString(color).replaceFirst("ff","#")
                        System.out.println("Color:" + selectedColor)
                    }
                })

            }
        }




    private fun showDeleteNoteDialog(){
        //if (dialogDeleteNote == null){
            var builder: AlertDialog.Builder = AlertDialog.Builder(this)
            var view: View = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    findViewById(R.id.layoutDeleteNoteCOntainer)
            )
            builder.setView(view)
            dialogDeleteNote = builder.create()
            if (dialogDeleteNote.window != null){
                dialogDeleteNote.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            view.findViewById<TextView>(R.id.textDeleteNote).setOnClickListener(){
                class DeleteNoteTask : AsyncTask<Void, Void, Void>() {
                    override fun doInBackground(vararg params: Void?): Void? {
                        var deletedNote: DeletedNote = DeletedNote()

                        deletedNote.setTitle(alreadyAvailableNote.getTitle())
                        deletedNote.setSubtitle(alreadyAvailableNote.getSubtitle())
                        deletedNote.setNoteText(alreadyAvailableNote.getNoteText())
                        deletedNote.setColor(alreadyAvailableNote.getColor())
                        deletedNote.setDateTime(alreadyAvailableNote.getDateTime())
                        deletedNote.setImagePath(alreadyAvailableNote.getImagePath())
                        deletedNote.setWebLink(alreadyAvailableNote.getWebLink())

                        getDatabase(applicationContext).DeletedNoteDao().insertDeletedNote(deletedNote)
                        NotesDatabase.db.getDatabase(applicationContext).noteDao()
                                .deleteNote(alreadyAvailableNote)
                        return null
                    }

                    override fun onPostExecute(result: Void?) {
                        super.onPostExecute(result)
                        var intent: Intent = Intent()
                        intent.putExtra("isNoteDeleted", true)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }

                DeleteNoteTask().execute()
            }
            view.findViewById<TextView>(R.id.textCancel).setOnClickListener(){
                dialogDeleteNote.dismiss()
            }
        //}
        dialogDeleteNote.show()
    }

    private fun setSubtitleIndicatorColor() {
        var gradientDrawable: GradientDrawable = viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedColor))
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Handling the result for the selected image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                var selectedImageUri: Uri? = data.data
                if (selectedImageUri != null) {
                    try {
                        var inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri)
                        var bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                        imageNote.setImageBitmap(bitmap)
                        imageNote.visibility = View.VISIBLE
                        findViewById<ImageView>(R.id.imageRemoveImage).visibility = View.VISIBLE

                        selectedImagePath = getPathFromUri(selectedImageUri)

                    } catch (exception: Exception) {
                        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    }
                }
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
                    textWebURL.setText(inputURL.text.toString())
                    layoutWebURL.visibility = View.VISIBLE
                    dialogAddURL.dismiss()
                }
            }
            view.findViewById<TextView>(R.id.textCancel).setOnClickListener(){
                dialogAddURL.dismiss()
            }
        //}
        dialogAddURL.show()
    }

}