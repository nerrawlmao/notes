# To-Do

A modern Android to-do list app with multi-note support, drag-and-drop tab reordering, dark mode, and a home screen widget — built entirely with Jetpack Compose and Material 3.

## Features

- **Multi-note management** — Create, edit, and delete multiple notes, each with its own list of to-do items
- **Tab-based navigation** — Notes are displayed as draggable tabs; tap to switch, long-press to reorder
- **Inline todo editing** — Add, edit, and remove items directly in the list
- **Smart input** — Pressing Enter while editing a todo creates a new item automatically
- **Checkbox toggles** — Mark items complete with a circular check; completed items show strikethrough text
- **Dark mode** — Toggle between light and dark themes from the top bar (persisted across sessions)
- **Home screen widget** — Glance-based widget shows your first note with toggle support (dark mode aware)
- **Persistent storage** — All data saved locally via Room (SQLite), survives app restarts

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + Repository) |
| Database | Room (SQLite) |
| DI / Navigation | Manual (no framework) |
| Widget | Glance AppWidget (`glance-appwidget`) |
| Drag & Drop | `composereorderable` |
| Language | Kotlin 2.2 |

## Architecture

The app follows a single-activity MVVM architecture:

```
UI (Compose) → NoteViewModel → NoteRepository → NoteDao → Room (SQLite)
```

- **MainActivity** — Single activity hosts all Compose screens
- **MainScreen** — Top-level composable with tabs, note content, and FAB
- **NoteViewModel** — Exposes `StateFlow<NoteEntity?>` for the active note; manages CRUD operations
- **NoteRepository** — Single source of truth, delegates to DAO
- **NoteDao** — Room DAO with Flow-based queries for reactive updates

## Screenshots

*(Add screenshots here)*

## Getting Started

### Prerequisites

- Android Studio (Ladybug or newer)
- JDK 17
- Android SDK 35

### Build & Run

1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/notes.git
   ```
2. Open the project in Android Studio
3. Sync Gradle and run on an emulator or device (minSdk 26)

### Build Variants

- `debug` — For development
- `release` — For production (requires a signing configuration)

## Project Structure

```
app/src/main/java/com/todo/notes/
├── MainActivity.kt          # Single activity entry point
├── ToDoApp.kt               # Application class
├── data/
│   ├── PrefsConstants.kt    # SharedPreferences keys
│   ├── db/
│   │   ├── AppDatabase.kt   # Room database
│   │   ├── NoteDao.kt       # Data access object
│   │   └── NoteEntity.kt    # Entity + TodoItem data class
│   └── repository/
│       └── NoteRepository.kt
├── ui/
│   ├── screens/
│   │   └── MainScreen.kt    # All composables (tabs, todos, FAB)
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/
│       └── NoteViewModel.kt
└── widget/
    └── TodoWidget.kt         # Glance widget + receiver
```

## License

This project is licensed under the MIT License.
