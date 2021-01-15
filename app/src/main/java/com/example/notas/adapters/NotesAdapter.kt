package com.example.notas.adapters

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notas.R
import com.example.notas.entities.Note
import com.example.notas.listeners.NotesListener
import com.makeramen.roundedimageview.RoundedImageView
import java.util.*
import java.util.logging.Handler

class NotesAdapter(notelist: List<Note>, notesListener: NotesListener) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {
    private var notes: List<Note>? = notelist
    var notesListener: NotesListener = notesListener
    private var timer: Timer? = null
    private var notesSource: List<Note>


    init {
        this.notes = notelist
        this.notesListener = notesListener
        notesSource = notelist
    }

    fun NotesAdapter(notes: List<Note>?, notesListener: NotesListener) {
        this.notes = notes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.setNote(notes!![position])
        holder.layoutNote.setOnClickListener(){
            notesListener.onNoteClicked(notes!!.get(position), position)
        }
    }

    override fun getItemCount(): Int {
        return notes!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textTitle: TextView
        var textSubtitle: TextView
        var textDateTime: TextView
        var textNoteText: TextView
        lateinit var layoutNote : LinearLayout
        lateinit var imageNote : RoundedImageView


        fun setNote(note: Note) {
            textTitle.text = note.getTitle()
            if (note.getSubtitle()!!.trim { it <= ' ' }.isEmpty()) {
                textSubtitle.visibility = View.GONE
            } else {
                textSubtitle.text = note.getSubtitle()
            }
            textDateTime.text = note.getDateTime()

            if (note.getNoteText()!!.trim().isEmpty()){
                textNoteText.visibility = View.GONE
            } else {
                textNoteText.text = note.getNoteText()
            }

            var gd: GradientDrawable = layoutNote.background as GradientDrawable
            gd.setColor(Color.parseColor(note.getColor()))
            System.out.println("Color implementado: " + note.getColor())

            if (note.getImagePath() != null) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()))
                imageNote.visibility = View.VISIBLE
            } else {
                imageNote.visibility = View.GONE
            }
        }

        init {
            textTitle = itemView.findViewById(R.id.textTitle)
            textSubtitle = itemView.findViewById(R.id.textSubtitle)
            textDateTime = itemView.findViewById(R.id.textDateTime)
            textNoteText = itemView.findViewById(R.id.textNoteText)
            layoutNote = itemView.findViewById(R.id.layoutNote)
            imageNote = itemView.findViewById(R.id.imageNote)
        }
    }

    fun searchNotes(searchKeyword: String){
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                if (searchKeyword.trim().isEmpty()) {
                   notes = notesSource
                } else {
                    var temp = ArrayList<Note>()
                    for (note : Note in notesSource) {
                        if (note.getTitle()!!.toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getSubtitle()!!.toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getNoteText()!!.toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(note)
                        }
                    }
                    notes = temp
                }
                val handler : android.os.Handler = android.os.Handler()
                val runnable = Runnable {
                    notifyDataSetChanged()
                }
                handler.post(runnable)
            }
        }, 500)
    }

    fun cancelTimer(){
        timer?.cancel()
    }

}