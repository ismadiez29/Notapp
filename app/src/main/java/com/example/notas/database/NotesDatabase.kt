package com.example.notas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notas.dao.NoteDao
import com.example.notas.entities.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
public abstract class NotesDatabase : RoomDatabase() {
    private var notesDatabase: NotesDatabase? = null

    @Synchronized
    open fun getDatabase(context: Context?): NotesDatabase? {
        if (notesDatabase == null) {
            notesDatabase = Room.databaseBuilder(
                    context!!,
                    NotesDatabase::class.java,
                    "notes_db"
            ).build()
        }
        return notesDatabase
    }
    abstract fun noteDao(): NoteDao?
}