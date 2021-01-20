package com.example.notas.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "deletedNotes")
class DeletedNote: Serializable {

    @PrimaryKey(autoGenerate = true)
    private var id = 0

    @ColumnInfo(name = "title")
    private var title: String? = null

    @ColumnInfo(name = "date_time")
    private var dateTime: String? = null

    @ColumnInfo(name = "subtitle")
    private var subtitle: String? = null

    @ColumnInfo(name = "note_text")
    private var noteText: String? = null

    @ColumnInfo(name = "image_path")
    private var imagePath: String? = null

    @ColumnInfo(name = "color")
    private var color: String? = null

    @ColumnInfo(name = "web_link")
    private var webLink: String? = null

    fun getId(): Int {
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun getDateTime(): String? {
        return dateTime
    }

    fun setDateTime(dateTime: String?) {
        this.dateTime = dateTime
    }

    fun getSubtitle(): String? {
        return subtitle
    }

    fun setSubtitle(subtitle: String?) {
        this.subtitle = subtitle
    }

    fun getNoteText(): String? {
        return noteText
    }

    fun setNoteText(noteText: String?) {
        this.noteText = noteText
    }

    fun getImagePath(): String? {
        return imagePath
    }

    fun setImagePath(imagePath: String?) {
        this.imagePath = imagePath
    }

    fun getColor(): String? {
        return color
    }

    fun setColor(color: String?) {
        this.color = color
    }

    fun getWebLink(): String? {
        return webLink
    }

    fun setWebLink(webLink: String?) {
        this.webLink = webLink
    }

    override fun toString(): String {
        return "$title : $dateTime"
    }

}