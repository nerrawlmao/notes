@file:SuppressLint("RestrictedApi")

package com.todo.notes.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.runBlocking
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.todo.notes.R
import com.todo.notes.data.PrefsConstants
import com.todo.notes.data.db.AppDatabase
import com.todo.notes.data.db.TodoItem

class TodoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val notes = db.noteDao().getAllNotesList()

        val prefs = context.getSharedPreferences(PrefsConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean(PrefsConstants.KEY_DARK_MODE, false)

        val currentNote = notes.firstOrNull()

        val textPrimary = if (isDark) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
        val textSecondary = if (isDark) Color(0xFF9E9E9E) else Color(0xFF9CA3AF)

        val bgRes = if (isDark) R.drawable.widget_bg_dark else R.drawable.widget_bg

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(bgRes))
                    .padding(12.dp)
            ) {
                if (currentNote == null) {
                    Text(
                        text = "No notes yet",
                        style = TextStyle(color = ColorProvider(textSecondary), fontSize = 14.sp)
                    )
                } else {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        Text(
                            text = currentNote.title.ifBlank { "Untitled" },
                            style = TextStyle(color = ColorProvider(textPrimary), fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            modifier = GlanceModifier.padding(bottom = 8.dp)
                        )
                        val todos = currentNote.todos
                        if (todos.isEmpty()) {
                            Text(text = "No todos", style = TextStyle(color = ColorProvider(textSecondary), fontSize = 13.sp))
                        } else {
                            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                                itemsIndexed(todos) { _, todo ->
                                    TodoWidgetItem(todo = todo, noteId = currentNote.id, isDark = isDark)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoWidgetItem(todo: TodoItem, noteId: Long, isDark: Boolean = false) {
    val textPrimary = if (isDark) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier.size(16.dp).clickable(
                onClick = actionRunCallback<TodoToggleAction>(
                    actionParametersOf(TodoToggleAction.NOTE_ID to noteId, TodoToggleAction.TODO_UID to todo.uid)
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            if (todo.completed) {
                Image(provider = ImageProvider(R.drawable.ic_check_filled), contentDescription = "Completed", modifier = GlanceModifier.size(16.dp))
            } else {
                Image(provider = ImageProvider(R.drawable.ic_check_empty), contentDescription = "Incomplete", modifier = GlanceModifier.size(16.dp))
            }
        }
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = todo.text.ifBlank { "Todo" },
            style = TextStyle(color = ColorProvider(textPrimary), fontSize = 13.sp, textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None),
            modifier = GlanceModifier.fillMaxWidth()
        )
    }
}

class TodoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = TodoWidget()

    override fun onReceive(context: Context, intent: Intent) {
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent.action) {
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: intArrayOf()
            if (appWidgetIds.isNotEmpty()) {
                updateWidgetWithRemoteViews(context, appWidgetIds)
            } else {
                super.onReceive(context, intent)
            }
        } else if ("com.todo.notes.TOGGLE_TODO" == intent.action) {
            val noteId = intent.getLongExtra("noteId", -1)
            val todoUid = intent.getStringExtra("todoUid") ?: return
            if (noteId >= 0) {
                handleToggleAction(context, noteId, todoUid)
            }
        } else {
            super.onReceive(context, intent)
        }
    }

    companion object {
        fun updateWidgetWithRemoteViews(context: Context, appWidgetIds: IntArray) {
            try {
                val db = AppDatabase.getDatabase(context)
                val notes = runBlocking { db.noteDao().getAllNotesList() }
                val prefs = context.getSharedPreferences(PrefsConstants.PREFS_NAME, Context.MODE_PRIVATE)
                val isDark = prefs.getBoolean(PrefsConstants.KEY_DARK_MODE, false)
                val currentNote = notes.firstOrNull()

                val bgColor = if (isDark) android.graphics.Color.parseColor("#1E1E1E") else android.graphics.Color.parseColor("#FFFFFF")
                val textColor = if (isDark) android.graphics.Color.parseColor("#E0E0E0") else android.graphics.Color.parseColor("#1A1A1A")
                val secondaryColor = if (isDark) android.graphics.Color.parseColor("#9E9E9E") else android.graphics.Color.parseColor("#9CA3AF")

                val appWidgetManager = AppWidgetManager.getInstance(context)

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_todo)

                    views.setInt(R.id.widget_container, "setBackgroundColor", bgColor)

                    val titleText = currentNote?.title?.ifBlank { "Untitled" } ?: "No notes yet"
                    views.setTextViewText(R.id.widget_title, titleText)
                    views.setTextColor(R.id.widget_title, textColor)

                    views.removeAllViews(R.id.widget_todos_container)

                    val todos = currentNote?.todos ?: emptyList()
                    if (todos.isEmpty()) {
                        val emptyView = RemoteViews(context.packageName, R.layout.widget_todo_item)
                        emptyView.setTextViewText(R.id.todo_text, "No todos")
                        emptyView.setTextColor(R.id.todo_text, secondaryColor)
                        emptyView.setViewVisibility(R.id.todo_checkbox, android.view.View.GONE)
                        views.addView(R.id.widget_todos_container, emptyView)
                    } else {
                        val note = currentNote!!
                        for (todo in todos) {
                            val itemView = RemoteViews(context.packageName, R.layout.widget_todo_item)
                            itemView.setTextViewText(R.id.todo_text, todo.text.ifBlank { "Todo" })
                            itemView.setTextColor(R.id.todo_text, textColor)

                            val checkRes = if (todo.completed) R.drawable.ic_check_filled else R.drawable.ic_check_empty
                            itemView.setImageViewResource(R.id.todo_checkbox, checkRes)
                            itemView.setViewVisibility(R.id.todo_checkbox, android.view.View.VISIBLE)

                            val clickIntent = Intent(context, TodoWidgetReceiver::class.java).apply {
                                action = "com.todo.notes.TOGGLE_TODO"
                                putExtra("noteId", note.id)
                                putExtra("todoUid", todo.uid)
                            }
                            val pendingIntent = PendingIntent.getBroadcast(
                                context, todo.uid.hashCode(), clickIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            itemView.setOnClickPendingIntent(R.id.todo_checkbox, pendingIntent)

                            views.addView(R.id.widget_todos_container, itemView)
                        }
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Log.e("TodoWidget", "updateWidgetWithRemoteViews error", e)
            }
        }

        fun handleToggleAction(context: Context, noteId: Long, todoUid: String) {
            val db = AppDatabase.getDatabase(context)
            val note = runBlocking { db.noteDao().getNoteById(noteId) } ?: return
            val updatedTodos = note.todos.map { todo ->
                if (todo.uid == todoUid) todo.copy(completed = !todo.completed) else todo
            }
            runBlocking { db.noteDao().updateNote(note.copy(todos = updatedTodos)) }

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TodoWidgetReceiver::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                updateWidgetWithRemoteViews(context, appWidgetIds)
            }
        }
    }
}

object TodoToggleAction : ActionCallback {
    val NOTE_ID = ActionParameters.Key<Long>("noteId")
    val TODO_UID = ActionParameters.Key<String>("todoUid")

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val noteId = parameters[NOTE_ID] ?: return
        val todoUid = parameters[TODO_UID] ?: return
        TodoWidgetReceiver.handleToggleAction(context, noteId, todoUid)
    }
}
