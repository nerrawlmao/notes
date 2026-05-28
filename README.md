<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" width="160" alt="To-Do Logo" style="border-radius: 50%;" />
</p>

<h1 align="center">To-Do</h1>

<p align="center">
  <em>A modern, feature-rich To-Do application for Android built with <strong>Kotlin</strong> and <strong>Jetpack Compose</strong>.</em>
</p>

<p align="center">
  <a href="app/build/outputs/apk/debug/To-Do.apk">Download APK</a>
</p>

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material_3-0061FF?style=for-the-badge&logo=materialdesign&logoColor=white)
![Room](https://img.shields.io/badge/Room-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Glance](https://img.shields.io/badge/Glance_Widget-34A853?style=for-the-badge&logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-26-FF6D00?style=for-the-badge&logo=android&logoColor=white)

## Features

- **Create Notes** — Tap the FAB to instantly create a new note with an empty todo item
- **Edit Titles** — Inline title editing with automatic save (500ms debounce)
- **Manage Todos** — Add, edit, and delete todo items within each note
- **Toggle Completion** — Tap the checkmark to mark items complete with animated strikethrough
- **Multi-Note Tabs** — Swipe between notes using a horizontal tab bar
- **Drag-to-Reorder Tabs** — Long-press and drag to reorder notes; position is persisted
- **Dark Mode** — Toggle dark/light theme from the top bar; setting is saved across sessions
- **Home Screen Widget** — Glance-based widget displays your first note's todos on the home screen with toggle support and adaptive dark/light backgrounds
- **Edge-to-Edge Display** — Full-screen experience using `enableEdgeToEdge()`

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.2.10 |
| **UI** | Jetpack Compose + Material 3 (Compose BOM 2025.06.00) |
| **Architecture** | MVVM (ViewModel + Repository + DAO) |
| **Database** | Room (SQLite) via KSP |
| **Async** | Kotlin Coroutines + StateFlow |
| **Widget** | Jetpack Glance 1.1.1 |
| **Drag & Drop** | `composereorderable` 0.9.6 |
| **Min / Target SDK** | 26 / 34 |

## License

```
MIT License

Copyright (c) 2026
```
