# 📝 Task Management App

A modern, intuitive, and efficient Android application designed to help users organize their daily lives. Built with a focus on clean UI, robust performance, and reliable task tracking.

---

## ✨ Key Features

*   **Full CRUD Functionality**: Easily create, view, update, and delete tasks.
*   **Intelligent Reminders**: Never miss a deadline with local notifications powered by AlarmManager.
*   **Dynamic Categories**: Organize tasks into custom categories (Work, Personal, Urgent, etc.) with a clean sidebar navigation.
*   **Advanced Filtering**: Filter tasks by "All", "Today", or "Overdue" status.
*   **Modern UI/UX**: 
    *   **Responsive Task Cards**: Auto-adjusting layout that handles long text and metadata gracefully.
    *   **Polished Splash Screen**: Professional entry with smooth fade-in and slide-up animations.
    *   **Dark Mode Support**: Full compatibility with system dark themes for comfortable night-time use.
*   **Local Database**: Powered by Room DB for fast, offline-first data persistence.

---

## 🛠️ Tech Stack

*   **Language**: Java (Modern Android standards)
*   **Database**: Room Persistence Library
*   **UI Components**: ConstraintLayout, Material Design Components, RecyclerView
*   **Architecture**: Model-View-Controller (MVC) with DAO patterns
*   **Background Tasks**: AlarmManager & BroadcastReceivers for precise scheduling

---

## 📸 Screen Previews

| Splash Screen | Task List | Task Details |
| :---: | :---: | :---: |
| Clean branding with animations | Organized list with status tags | Comprehensive task info |

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio (Latest Version)
*   JDK 11 or higher
*   Android Device or Emulator (API 24+)

### Installation
1.  **Clone the Repository**
    ```bash
    git clone https://github.com/pouvsaroth/TaskManagementApp.git
    ```
2.  **Open in Android Studio**
    *   Navigate to the project folder and click `Open`.
3.  **Sync Gradle**
    *   Wait for the project to build and download necessary dependencies.
4.  **Run the App**
    *   Select your device and click the **Run** button (green play icon).

---

## 🛠️ Recent Improvements
*   ✅ Fixed Task Card overlapping issues for long content.
*   ✅ Optimized Notification system with white-silhouette icons for system compatibility.
*   ✅ Redesigned Splash Screen for a more professional first impression.
*   ✅ Resolved Room Database constructor warnings for improved stability.

---

## 🤝 Contribution
Contributions are welcome! Feel free to open an issue or submit a pull request if you have ideas to improve the app.

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
