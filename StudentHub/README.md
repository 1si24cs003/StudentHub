# 🎓 StudentHub - Smart Academic & Task Manager

**StudentHub** is an Android app built with **Jetpack Compose** and **Kotlin** that helps students track subjects, modules, exams, and personal notes using a **Monday.com-style board** and an autonomous **Gemini AI Assistant**.

---

## ✨ Features

- 📊 **Monday.com-Style Academics Board**: Organize subjects, modules, and chapters in a hierarchical table layout with customizable status tags (*Working on it*, *Stuck*, *Done*).
- 🗓️ **Start Dates & Calendar Picker**: Assign start dates to individual modules with an integrated Material 3 Date Picker.
- 🤖 **Gemini AI Assistant**: Autonomous AI with function calling capabilities. Upload syllabus photos or schedules, and Gemini will parse and populate your subjects and exams automatically!
- 🔔 **Smart Exam Reminders**: Automated notifications 2 days and 1 day prior to upcoming exams.
- ⏰ **Daily 6 PM Study Summary**: Automated background alarm giving you a daily summary of modules in progress and remaining tasks.
- 📝 **Personal Notes with Alarms**: Take notes and schedule instant/custom reminders.
- 🔐 **Privacy First & Bring Your Own Key (BYOK)**: Enter your own free Gemini API Key securely saved in local `SharedPreferences`.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: Jetpack Compose (Material 3)
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Database**: Room Database with Coroutines & StateFlow
- **AI SDK**: Google AI Client SDK (`com.google.ai.client.generativeai`)
- **Background Tasks**: `AlarmManager` + `BroadcastReceiver`
- **Navigation**: Jetpack Navigation Compose

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- Android SDK 34+
- A free Gemini API Key from [Google AI Studio](https://aistudio.google.com/)

### Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/StudentHub.git
   cd StudentHub
   ```

2. **Open in Android Studio**:
   - Open Android Studio, select **Open**, and navigate to the cloned project folder.

3. **Build & Run**:
   - Connect an Android device or start an emulator.
   - Click **Run** (`Shift + F10`).

4. **Add your Gemini API Key**:
   - Open the **AI Assistant** tab in the app.
   - Tap the **Settings (Gear Icon)** in the top right corner.
   - Paste your free Gemini API Key from Google AI Studio and tap **Save**.

---

## 📄 License

```
MIT License

Copyright (c) 2026 Abdullah

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

*Made with ❤️ by Abdullah*
