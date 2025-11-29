# Trackr 🚀

**Trackr** is a modern, feature-rich IT Help Desk and Ticketing application built for Android using **Kotlin** and **Jetpack Compose**. It is designed to streamline support workflows with advanced features like automated SLA monitoring, smart ticket grouping, and comprehensive analytics dashboards.

## 📱 Screenshots

| Login Screen | User Dashboard | Grouped Tickets | SLA Configuration |
|<img width="484" height="419" alt="image" src="https://github.com/user-attachments/assets/21a85ca7-a3c6-4106-97dd-f650d8931d9e" />|


## ✨ Key Features

### 🎫 Ticket Management

  * **CRUD Operations:** Create, read, update, and delete support tickets.
  * **Real-time Updates:** Powered by **Cloud Firestore** for instant data synchronization.
  * **Smart Filtering:** Filter tickets by Status, Priority, or Search by text.
  * **Ticket Grouping Engine:** Automatically identifies and groups similar tickets (e.g., "Printer Jam") to help technicians resolve mass issues faster.

### 🧠 Knowledge Base (KB)

  * **Article Management:** View and manage help articles.
  * **Deflection Tracking:** Tracks search gaps and article effectiveness.
  * **Smart Linking:** Link KB articles directly to tickets to assist resolution.

### ⏱️ SLA Engine (Service Level Agreement)

  * **Automated Monitoring:** A background `WorkManager` job runs periodically to check ticket ages against configured rules.
  * **Status Progression:** Automatically updates tickets from `OK` → `Warning` → `Breached`.
  * **Configurable Rules:** Admins can define custom time limits (in hours) for different priority levels (Low, Medium, High, Urgent).

### 🔔 Notification System

  * **Local Alerts:** Immediate vibration and system notifications for SLA breaches.
  * **Push Notifications:** Integrated with **Firebase Cloud Messaging (FCM)** for remote updates (e.g., "Ticket Assigned").
  * **Android 13+ Support:** Fully compliant with modern permission runtime requirements.

### 📊 Dashboards & Analytics

  * **Role-Based Access:** Distinct dashboards for **Admins**, **Managers**, and **Users**.
  * **Visual Charts:**
      * Pie Charts for Ticket Categories.
      * Bar Charts for Ticket Aging.
      * Line Charts for Resolution trends.
  * **Quality Metrics:** Tracks **CSAT (Customer Satisfaction)** scores, Reopen Rates, and Resolution Times.
  * **Reporting:** Export detailed CSV reports for users and tickets.

## 🛠️ Tech Stack

  * **Language:** [Kotlin](https://kotlinlang.org/)
  * **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material3)
  * **Architecture:** MVVM with Clean Architecture (Domain/Data/UI layers)
  * **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)
  * **Asynchronous:** [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
  * **Navigation:** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
  * **Backend / Database:**
      * [Firebase Auth](https://firebase.google.com/docs/auth) (Email/Password)
      * [Cloud Firestore](https://firebase.google.com/docs/firestore) (NoSQL Database)
      * [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging) (Push Notifications)
  * **Local Storage:** [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences)
  * **Background Work:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
  * **Charting:** `tehras/charts` library

## 🏗️ Architecture

The app follows the recommended **Guide to App Architecture**:

1.  **Domain Layer:** Contains Business Logic, Use Cases, and Repository Interfaces. (e.g., `GroupingEngine`, `SLAWorker`)
2.  **Data Layer:** Contains Repository Implementations, Data Sources (Firestore), and API logic.
3.  **UI Layer:** Contains ViewModels and Composable screens.

## 🚀 Getting Started

### Prerequisites

  * Android Studio Ladybug (or newer)
  * JDK 17 or higher

### Setup Instructions

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/AlphaZ27/trackr.git
    ```
2.  **Firebase Setup:**
      * Create a project in the [Firebase Console](https://console.firebase.google.com/).
      * Add an Android app with package name `com.example.trackr`.
      * Download `google-services.json` and place it in the `app/` directory.
      * Enable **Authentication** (Email/Password).
      * Enable **Firestore Database**.
3.  **Build and Run:**
      * Open the project in Android Studio.
      * Sync Gradle files.
      * Run on an Emulator or Physical Device.

## 🧪 Testing

To run unit tests:

```bash
./gradlew test
```

To run instrumentation tests:

```bash
./gradlew connectedAndroidTest
```


## 📄 License

This project is licensed under the MIT License - see the [LICENSE](https://www.google.com/search?q=LICENSE) file for details.
