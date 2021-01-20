package com.example.notas.dao

import androidx.room.*
import com.example.notas.entities.DeletedNote

@Dao
interface DeletedNoteDao {

    @Query("SELECT * FROM deletedNotes ORDER BY id DESC")
    fun getAllDeletedNotes(): List<DeletedNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDeletedNote(note: DeletedNote)

    @Delete
    fun removeDeletedNote(note: DeletedNote)
}