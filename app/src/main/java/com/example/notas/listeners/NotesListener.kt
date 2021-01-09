package com.example.notas.listeners

import com.example.notas.entities.Note

interface NotesListener {

    fun onNoteClicked(note: Note, position: Int){

    }

}