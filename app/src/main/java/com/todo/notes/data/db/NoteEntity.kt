package com.todo.notes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Entity(tableName = "notes")
@TypeConverters(TodoListConverter::class)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "Untitled",
    val todos: List<TodoItem> = listOf(TodoItem("", false)),
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class TodoItem(
    val text: String = "",
    val completed: Boolean = false,
    val uid: String = UUID.randomUUID().toString()
)

class TodoListConverter {
    companion object {
        private const val TAG = "TodoListConverter"
    }

    @TypeConverter
    fun fromTodoList(value: List<TodoItem>): String {
        val jsonArray = JSONArray()
        value.forEach { item ->
            val obj = JSONObject()
            obj.put("text", item.text)
            obj.put("completed", item.completed)
            obj.put("uid", item.uid)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toTodoList(value: String): List<TodoItem> {
        val list = mutableListOf<TodoItem>()
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TodoItem(
                        text = obj.getString("text"),
                        completed = obj.getBoolean("completed"),
                        uid = obj.optString("uid", UUID.randomUUID().toString())
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse todo list JSON", e)
        }
        return list
    }
}
