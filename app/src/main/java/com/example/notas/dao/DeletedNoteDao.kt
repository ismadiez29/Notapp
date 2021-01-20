package com.example.notas.dao

import androidx.room.*
import com.example.notas.entities.Note

@Dao
interface DeletedNoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllDeletedNotes(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDeletedNote(note: Note)

    @Delete
    fun removeDeletedNote(note: Note)
}