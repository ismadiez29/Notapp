package com.example.notas.adapters

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notas.R
import com.example.notas.entities.DeletedNote
import com.example.notas.listeners.NotesListener
import com.makeramen.roundedimageview.RoundedImageView

class DeletedNotesAdapter (notelist: List<DeletedNote>, notesListener: NotesListener) : RecyclerView.Adapter<DeletedNotesAdapter.DeleteNoteViewHolder>() {
    private var deletedNotes: List<DeletedNote>? = notelist
    var notesListener: NotesListener = notesListener

    init {
        this.deletedNotes = notelist
        this.notesListener = notesListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeletedNotesAdapter.DeleteNoteViewHolder {
        return DeletedNotesAdapter.DeleteNoteViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holderDelete: DeletedNotesAdapter.DeleteNoteViewHolder, position: Int) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        holderDelete.setNote(deletedNotes!![position])
        holderDelete.layoutNote.setOnClickListener(){
            notesListener.onDeletedNoteClicked(deletedNotes!!.get(position), position)
        }    }

    override fun getItemCount(): Int {
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        System.out.println(deletedNotes?.size)
        return deletedNotes!!.size
    }

    class DeleteNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textTitle: TextView
        var textSubtitle: TextView
        var textDateTime: TextView
        var textNoteText: TextView
        lateinit var layoutNote : LinearLayout
        lateinit var imageNote : RoundedImageView


        fun setNote(note: DeletedNote) {
            System.out.println("------------------------------------------------------")
            textTitle.text = note.getTitle()
/*            if (note.getSubtitle()!!.trim { it <= ' ' }.isEmpty()) {
                textSubtitle.visibility = View.GONE
            } else {
                textSubtitle.text = note.getSubtitle()
            }
            textDateTime.text = note.getDateTime()

            if (note.getNoteText()!!.trim().isEmpty()){
                textNoteText.visibility = View.GONE
            } else {
                textNoteText.text = note.getNoteText()
            }*/

            var gd: GradientDrawable = layoutNote.background as GradientDrawable
            gd.setColor(Color.parseColor(note.getColor()))

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
}