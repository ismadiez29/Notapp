package com.example.notas.dao

import androidx.room.*
import com.example.notas.entities.ArchivedNote
import com.example.notas.entities.DeletedNote

@Dao
interface ArchivedNoteDao {

    @Query("SELECT * FROM archivedNotes ORDER BY id DESC")
    fun getAllArchivedNotes(): List<ArchivedNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArchivedNote(note: ArchivedNote)

    @Delete
    fun removeArchivedNote(note: ArchivedNote)

}