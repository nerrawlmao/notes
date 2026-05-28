package com.todo.notes.ui.viewmodel

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.todo.notes.data.db.AppDatabase
import com.todo.notes.data.db.NoteEntity
import com.todo.notes.data.db.TodoItem
import com.todo.notes.data.PrefsConstants
import com.todo.notes.data.repository.NoteRepository
import com.todo.notes.widget.TodoWidgetReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository

    val notes: StateFlow<List<NoteEntity>>
    private val _selectedNoteId = MutableStateFlow<Long?>(null)
    val selectedNoteId: StateFlow<Long?> = _selectedNoteId.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private var titleDebounceJob: Job? = null

    private var initialized = false

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao())

        notes = repository.allNotes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        val prefs = getApplication<Application>()
            .getSharedPreferences(PrefsConstants.PREFS_NAME, Context.MODE_PRIVATE)
        _isDarkMode.value = prefs.getBoolean(PrefsConstants.KEY_DARK_MODE, false)

        viewModelScope.launch {
            notes.drop(1).collect { noteList ->
                if (!initialized) {
                    initialized = true
                    if (noteList.isNotEmpty()) {
                        val id = noteList.first().id
                        _selectedNoteId.value = id
                        saveLastNoteId(id)
                    }
                } else if (_selectedNoteId.value != null && noteList.none { it.id == _selectedNoteId.value }) {
                    if (noteList.isNotEmpty()) {
                        val newId = noteList.first().id
                        _selectedNoteId.value = newId
                        saveLastNoteId(newId)
                    } else {
                        _selectedNoteId.value = null
                    }
                }
            }
        }
    }

    fun selectNote(id: Long) {
        if (_selectedNoteId.value != id) {
            _selectedNoteId.value = id
            saveLastNoteId(id)
            updateWidget()
        }
    }

    fun createNewNote() {
        viewModelScope.launch {
            val newNote = NoteEntity(
                title = "Untitled",
                todos = listOf(TodoItem("", false))
            )
            val id = repository.insertNote(newNote)
            _selectedNoteId.value = id
            saveLastNoteId(id)
            updateWidget()
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            val currentNotes = notes.value.toList()
            repository.deleteNoteById(id)
            val remaining = currentNotes.filter { it.id != id }
            if (remaining.isNotEmpty()) {
                if (_selectedNoteId.value == id) {
                    val deletedIndex = currentNotes.indexOfFirst { it.id == id }
                    val nextIndex = if (deletedIndex < remaining.size) deletedIndex
                                    else remaining.lastIndex
                    val nextId = remaining[nextIndex].id
                    _selectedNoteId.value = nextId
                    saveLastNoteId(nextId)
                }
            } else {
                _selectedNoteId.value = null
                saveLastNoteId(-1L)
            }
            updateWidget()
        }
    }

    fun persistTabOrder(orderedNoteIds: List<Long>) {
        viewModelScope.launch {
            repository.reorderNotes(orderedNoteIds)
            _selectedNoteId.value?.let { saveLastNoteId(it) }
            updateWidget()
        }
    }

    fun updateTitle(id: Long, title: String) {
        viewModelScope.launch {
            repository.updateTitle(id, title)
        }
        titleDebounceJob?.cancel()
        titleDebounceJob = viewModelScope.launch {
            delay(500)
            updateWidget()
        }
    }

    fun toggleTodo(noteId: Long, todoIndex: Int) {
        viewModelScope.launch {
            repository.toggleTodo(noteId, todoIndex)
            updateWidget()
        }
    }

    fun updateTodoText(noteId: Long, todoIndex: Int, text: String) {
        viewModelScope.launch {
            repository.updateTodoText(noteId, todoIndex, text)
            updateWidget()
        }
    }

    fun addTodoItem(noteId: Long) {
        viewModelScope.launch {
            repository.addTodoItem(noteId)
            updateWidget()
        }
    }

    fun removeTodoItem(noteId: Long, todoIndex: Int) {
        viewModelScope.launch {
            repository.removeTodoItem(noteId, todoIndex)
            updateWidget()
        }
    }

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        val prefs = getApplication<Application>()
            .getSharedPreferences(PrefsConstants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(PrefsConstants.KEY_DARK_MODE, newValue).commit()
        updateWidget()
    }

    private fun updateWidget() {
        try {
            val context = getApplication<Application>()
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TodoWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                TodoWidgetReceiver.updateWidgetWithRemoteViews(context, appWidgetIds)
            }
        } catch (e: Exception) {
            Log.e("TodoWidget", "updateWidget error", e)
        }
    }

    private fun saveLastNoteId(id: Long) {
        val prefs = getApplication<Application>().getSharedPreferences(PrefsConstants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(PrefsConstants.KEY_LAST_NOTE_ID, id).apply()
    }

}
