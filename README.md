<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="120" height="120" alt="Notes Logo">
</p>

<h1 align="center">Notes</h1>

<p align="center">
  <em>A modern note-taking app built with Jetpack Compose & Material 3</em>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#screenshots">Screenshots</a> •
  <a href="#download">Download</a> •
  <a href="#tech-stack">Tech Stack</a> •
  <a href="#building">Building</a>
</p>

---

## ✨ Features

- ✅ **Create, edit & delete notes** — simple and fast
- 🔄 **Drag-to-reorder** tabs and notes
- 🏷️ **Tab-based organization** — group notes by category
- 🔍 **Search** through your notes
- 🎨 **Material 3 Design** — dynamic theming
- 📱 **Home screen widget** — glance at your todos (Glance App Widget)
- 💾 **Persistent storage** via Room database
- 🌙 **Dark mode** support

## 📥 Download

| Variant | APK |
|---------|-----|
| Debug   | [📦 notes.apk](app/build/outputs/apk/debug/notes.apk) |

> The APK above is the latest debug build. It requires **Android 8.0+ (API 26)**.

## 🧱 Tech Stack

| Layer | Technology |
|-------|-----------|
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM (ViewModel + Repository) |
| **Database** | Room (SQLite) with KSP |
| **State** | StateFlow + Compose runtime |
| **Widget** | Glance AppWidget + Material 3 |
| **Reorder** | `composereorderable` |
| **Build** | Gradle KTS + Kotlin 2.x |

## 🛠️ Building

```bash
# Clone the repo
git clone <repo-url>
cd Notes

# Build debug APK
./gradlew assembleDebug
```

The debug APK will be at:

```
app/build/outputs/apk/debug/notes.apk
```

---

<p align="center">
  Made with ☕ & Kotlin
</p>
