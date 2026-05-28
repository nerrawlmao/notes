package com.todo.notes.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todo.notes.data.db.NoteEntity
import com.todo.notes.data.db.TodoItem
import com.todo.notes.ui.viewmodel.NoteViewModel
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.collectAsState
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun MainScreen(viewModel: NoteViewModel) {
    val notes by viewModel.notes.collectAsState()
    val selectedId by viewModel.selectedNoteId.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    val selectedNote = notes.find { it.id == selectedId }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopBar(
                isDark = isDark,
                onToggleDark = { viewModel.toggleDarkMode() }
            )

            TabBar(
                notes = notes,
                selectedNoteId = selectedId,
                onTabSelected = { viewModel.selectNote(it) },
                onTabDelete = { viewModel.deleteNote(it) },
                onDragEndReorder = { orderedIds -> viewModel.persistTabOrder(orderedIds) }
            )

            if (selectedNote != null) {
                NoteContent(
                    note = selectedNote,
                    onTitleChange = { viewModel.updateTitle(selectedNote.id, it) },
                    onTodoToggle = { index -> viewModel.toggleTodo(selectedNote.id, index) },
                    onTodoTextChange = { index, text -> viewModel.updateTodoText(selectedNote.id, index, text) },
                    onAddTodo = { viewModel.addTodoItem(selectedNote.id) },
                    onRemoveTodo = { index -> viewModel.removeTodoItem(selectedNote.id, index) }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add a new note",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.createNewNote() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Note",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun TopBar(
    isDark: Boolean,
    onToggleDark: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "To-Do",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = onToggleDark) {
            Icon(
                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle dark mode",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun TabBar(
    notes: List<NoteEntity>,
    selectedNoteId: Long?,
    onTabSelected: (Long) -> Unit,
    onTabDelete: (Long) -> Unit,
    onDragEndReorder: (List<Long>) -> Unit
) {
    val listState = rememberLazyListState()

    var tabItems by remember { mutableStateOf(notes) }
    var currentlyDragging by remember { mutableStateOf(false) }

    LaunchedEffect(notes) {
        if (!currentlyDragging) {
            tabItems = notes
        }
    }

    val reorderState = rememberReorderableLazyListState(
        listState = listState,
        onMove = { from, to ->
            currentlyDragging = true
            tabItems = tabItems.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            true
        },
        onDragEnd = { _, _ ->
            onDragEndReorder(tabItems.map { it.id })
            currentlyDragging = false
        }
    )

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .reorderable(reorderState),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(tabItems, key = { it.id }) { note ->
            ReorderableItem(
                state = reorderState,
                key = note.id
            ) { isDragging ->
                val isSelected = note.id == selectedNoteId
                val bgColor by animateColorAsState(
                    targetValue = when {
                        isDragging -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else -> Color.Transparent
                    },
                    label = "tabBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary,
                    label = "tabText"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor, RoundedCornerShape(12.dp))
                        .detectReorderAfterLongPress(reorderState)
                        .pointerInput(note.id) {
                            detectTapGestures { onTabSelected(note.id) }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.title.ifBlank { "Untitled" },
                            style = MaterialTheme.typography.labelLarge,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                        if (notes.size > 1) {
                            IconButton(
                                onClick = { onTabDelete(note.id) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete note",
                                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun NoteContent(
    note: NoteEntity,
    onTitleChange: (String) -> Unit,
    onTodoToggle: (Int) -> Unit,
    onTodoTextChange: (Int, String) -> Unit,
    onAddTodo: () -> Unit,
    onRemoveTodo: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            var titleValue by remember(note.id) {
                mutableStateOf(TextFieldValue(note.title, TextRange(note.title.length)))
            }

            BasicTextField(
                value = titleValue,
                onValueChange = {
                    titleValue = it
                    onTitleChange(it.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 26.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (titleValue.text.isEmpty()) {
                            Text(
                                text = "Note title...",
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                    fontSize = 26.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(note.todos) { index, todo ->
                TodoItemCard(
                    index = index,
                    todo = todo,
                    onToggle = { onTodoToggle(index) },
                    onTextChange = { text -> onTodoTextChange(index, text) },
                    onRemove = { onRemoveTodo(index) },
                    onAddAfter = { onAddTodo() },
                    canRemove = note.todos.size > 1
                )
            }

            if (note.todos.isEmpty()) {
                item {
                    Text(
                        text = "Add a todo item",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddTodo() }
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add todo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add item",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun TodoItemCard(
    index: Int,
    todo: TodoItem,
    onToggle: () -> Unit,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    onAddAfter: () -> Unit,
    canRemove: Boolean
) {
    var textFieldValue by remember(todo.text, index) {
        mutableStateOf(TextFieldValue(todo.text, TextRange(todo.text.length)))
    }
    var isFocused by remember { mutableStateOf(false) }

    val currentText = textFieldValue.text

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (todo.completed) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (todo.completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        if (newValue.text.contains('\n')) {
                            val cleanText = newValue.text.replace("\n", "")
                            textFieldValue = TextFieldValue(
                                text = cleanText,
                                selection = TextRange(cleanText.length)
                            )
                            onTextChange(cleanText)
                            onAddAfter()
                        } else {
                            textFieldValue = newValue
                            onTextChange(newValue.text)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box {
                            if (textFieldValue.text.isEmpty() && !isFocused) {
                                Text(
                                    text = "Todo item...",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                        fontSize = 16.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            if (canRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove todo",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
