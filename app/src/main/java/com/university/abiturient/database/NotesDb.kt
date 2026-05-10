package com.university.abiturient.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.university.abiturient.model.Note

class NotesDb(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE note (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                body TEXT NOT NULL,
                image_path TEXT,
                created_at INTEGER NOT NULL
            );
            """.trimIndent()
        )
        seedDemo(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS note")
        onCreate(db)
    }

    fun getAllNotes(): List<Note> {
        readableDatabase.rawQuery(
            "SELECT id, title, body, image_path, created_at FROM note ORDER BY created_at DESC",
            null
        ).use { c ->
            val list = mutableListOf<Note>()
            while (c.moveToNext()) {
                list.add(readNote(c))
            }
            return list
        }
    }

    fun getNotesWithImage(): List<Note> {
        readableDatabase.rawQuery(
            """
            SELECT id, title, body, image_path, created_at
            FROM note
            WHERE image_path IS NOT NULL AND image_path != ''
            ORDER BY created_at DESC
            """.trimIndent(),
            null
        ).use { c ->
            val list = mutableListOf<Note>()
            while (c.moveToNext()) {
                list.add(readNote(c))
            }
            return list
        }
    }

    fun getNote(id: Long): Note? {
        readableDatabase.rawQuery(
            "SELECT id, title, body, image_path, created_at FROM note WHERE id = ?",
            arrayOf(id.toString())
        ).use { c ->
            if (!c.moveToFirst()) return null
            return readNote(c)
        }
    }

    fun insertNote(title: String, body: String, imagePath: String?): Long {
        val cv = ContentValues().apply {
            put("title", title)
            put("body", body)
            put("image_path", imagePath)
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert("note", null, cv)
    }

    fun updateNote(id: Long, title: String, body: String, imagePath: String?) {
        val cv = ContentValues().apply {
            put("title", title)
            put("body", body)
            put("image_path", imagePath)
        }
        writableDatabase.update("note", cv, "id = ?", arrayOf(id.toString()))
    }

    fun deleteNote(id: Long) {
        writableDatabase.delete("note", "id = ?", arrayOf(id.toString()))
    }

    private fun readNote(c: android.database.Cursor): Note {
        return Note(
            id = c.getLong(0),
            title = c.getString(1),
            body = c.getString(2),
            imagePath = c.getString(3)?.takeIf { it.isNotBlank() },
            createdAt = c.getLong(4)
        )
    }

    private fun seedDemo(db: SQLiteDatabase) {
        val cv = ContentValues().apply {
            put("title", "Первая заметка")
            put(
                "body",
                "Пример заметки при первом запуске. Нажмите «+», чтобы создать новую, или откройте эту для редактирования."
            )
            putNull("image_path")
            put("created_at", System.currentTimeMillis())
        }
        db.insert("note", null, cv)
    }

    companion object {
        private const val DATABASE_NAME = "notes.db"
        private const val DATABASE_VERSION = 1
    }
}
