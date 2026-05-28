package com.todo.notes.data.repository

import com.todo.notes.data.db.NoteDao
import com.todo.notes.data.db.NoteEntity
import com.todo.notes.data.db.TodoItem
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity): Long {
        val maxPos = noteDao.getMaxPosition() ?: -1
        val noteWithPos = note.copy(position = maxPos + 1)
        return noteDao.insertNote(noteWithPos)
    }

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    suspend fun reorderNotes(noteIds: List<Long>) {
        noteIds.forEachIndexed { index, id ->
            noteDao.updateNotePosition(id, index)
        }
    }

    suspend fun updateTitle(id: Long, title: String) {
        val note = noteDao.getNoteById(id) ?: return
        noteDao.updateNote(note.copy(title = title))
    }

    suspend fun updateTodos(id: Long, todos: List<TodoItem>) {
        val note = noteDao.getNoteById(id) ?: return
        noteDao.updateNote(note.copy(todos = todos))
    }

    suspend fun addTodoItem(id: Long) {
        val note = noteDao.getNoteById(id) ?: return
        val updatedTodos = note.todos + TodoItem("", false)
        noteDao.updateNote(note.copy(todos = updatedTodos))
    }

    suspend fun removeTodoItem(id: Long, todoIndex: Int) {
        val note = noteDao.getNoteById(id) ?: return
        if (note.todos.size > 1) {
            val updatedTodos = note.todos.toMutableList().apply { removeAt(todoIndex) }
            noteDao.updateNote(note.copy(todos = updatedTodos))
        }
    }

    suspend fun toggleTodo(id: Long, todoIndex: Int) {
        val note = noteDao.getNoteById(id) ?: return
        val updatedTodos = note.todos.toMutableList().apply {
            if (todoIndex < size) {
                val current = this[todoIndex]
                this[todoIndex] = current.copy(completed = !current.completed)
            }
        }
        noteDao.updateNote(note.copy(todos = updatedTodos))
    }

    suspend fun toggleTodoByUid(noteId: Long, todoUid: String) {
        val note = noteDao.getNoteById(noteId) ?: return
        val updatedTodos = note.todos.map { todo ->
            if (todo.uid == todoUid) todo.copy(completed = !todo.completed) else todo
        }
        noteDao.updateNote(note.copy(todos = updatedTodos))
    }

    suspend fun updateTodoText(id: Long, todoIndex: Int, text: String) {
        val note = noteDao.getNoteById(id) ?: return
        val updatedTodos = note.todos.toMutableList().apply {
            if (todoIndex < size) {
                this[todoIndex] = this[todoIndex].copy(text = text)
            }
        }
        noteDao.updateNote(note.copy(todos = updatedTodos))
    }

    suspend fun getNoteCount(): Int = noteDao.getNoteCount()
}
