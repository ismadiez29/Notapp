package com.example.notas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notas.dao.ArchivedNoteDao
import com.example.notas.dao.DeletedNoteDao
import com.example.notas.dao.NoteDao
import com.example.notas.entities.ArchivedNote
import com.example.notas.entities.DeletedNote
import com.example.notas.entities.Note

@Database(entities = [Note::class, DeletedNote::class, ArchivedNote::class], version = 2, exportSchema = false)
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
    abstract fun DeletedNoteDao(): DeletedNoteDao
    abstract fun ArchivedNoteDao(): ArchivedNoteDao

}