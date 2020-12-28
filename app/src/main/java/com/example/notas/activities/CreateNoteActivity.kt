package com.example.notas.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.example.notas.R

class CreateNoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?){
                onBackPressed()
            }
        })
    }
}