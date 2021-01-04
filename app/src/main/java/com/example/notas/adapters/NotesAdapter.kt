package com.example.notas.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notas.R
import com.example.notas.entities.Note

class NotesAdapter(notelist: ArrayList<Note>) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {
    private var notes: List<Note>? = notelist

    fun NotesAdapter(notes: List<Note>?) {
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
        fun setNote(note: Note) {
            textTitle.text = note.getTitle()
            if (note.getSubtitle()!!.trim { it <= ' ' }.isEmpty()) {
                textSubtitle.visibility = View.GONE
            } else {
                textSubtitle.text = note.getSubtitle()
            }
            textDateTime.text = note.getDateTime()
        }

        init {
            textTitle = itemView.findViewById(R.id.textTitle)
            textSubtitle = itemView.findViewById(R.id.textSubtitle)
            textDateTime = itemView.findViewById(R.id.textDateTime)
        }
    }
}