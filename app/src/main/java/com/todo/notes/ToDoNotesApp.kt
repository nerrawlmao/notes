package com.todo.notes

import android.app.Application
import com.todo.notes.data.db.AppDatabase

class ToDoNotesApp : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ToDoNotesApp
            private set
    }
}
