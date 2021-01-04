package com.example.notas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notas.dao.NoteDao
import com.example.notas.entities.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {

    var notesDatabase : NotesDatabase? = null
 object db{
    //lateinit var notesDatabase: NotesDatabase
    fun getDatabase(context: Context): NotesDatabase {

        var notesDB = Room.databaseBuilder(
                context,
                NotesDatabase::class.java,
                "notes_db"
        ).build()
        return notesDB
    }
}

    fun getDatabase(context: Context): NotesDatabase? {
            notesDatabase = Room.databaseBuilder(
                    context,
                    NotesDatabase::class.java,
                    "notes_db"
            ).build()

        return notesDatabase
    }
    abstract fun noteDao(): NoteDao
}