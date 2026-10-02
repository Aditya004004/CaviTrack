# CaviTrack Android App

CaviTrack is a production-grade, native Android inventory management app built with Kotlin, Jetpack Compose, and Material 3 for tracking physical components, customers, molds, and action history.

## Architecture

The app follows **Clean Architecture** and **MVVM/MVI** patterns using modern Android Jetpack libraries:

- **Presentation Layer**: Built 100% in Jetpack Compose using Unidirectional Data Flow (UDF) via sealed `UiState` and `StateFlow`.
  - Type-safe Compose Navigation via Kotlin Serialization routes.
  - Paging 3 integration (`FirestorePagingSource`) for indexed, chunked collection pagination.
  - CameraX integration with automatic downscaling and bitmap recycling.
- **Domain Layer**:
  - Pure Kotlin domain entities (`Component`, `Customer`, `Mold`, `HistoryLog`).
  - Abstract repository contracts (`InventoryRepository`, `AuthRepository`).
  - Orchestration use cases for critical flows (authentication lifecycle, metric aggregation, and account deletion compliance).
- **Data Layer**:
  - **Local Metrics Cache**: Jetpack DataStore Preferences (`LocalMetricsRepository`) provides instant, zero-latency dashboard metric rendering on cold launch before network refresh.
  - **Remote Database & Offline Persistence**: Cloud Firestore with native offline persistence enabled (`PersistentCacheSettings`), caching documents and queuing offline writes with automatic SDK synchronization upon reconnection.
  - **Authentication**: Firebase Authentication with automated session tracking, 5-minute re-authentication validation for sensitive data/account deletion operations.
  - **Storage**: Firebase Cloud Storage with client-side image compression and size validation.
  - **Background Work**: Jetpack WorkManager (`TokenSyncWorker`) for periodic FCM push token synchronization.
  - **Observability**: Firebase Crashlytics & Firebase Analytics for real-time crash reporting and telemetry.
  - **Security & Integrity**: Firebase App Check (Play Integrity in release, Debug Provider in debug) and fine-grained Firestore security rules.

## Setup & Requirements

### Firebase Configuration
1. Register this app (`com.company.cavitrack`) on the [Firebase Console](https://console.firebase.google.com/).
2. Enable Firestore, Firebase Authentication (Email/Password), Cloud Storage, and Crashlytics.
3. Download the `google-services.json` file.
4. Place `google-services.json` in the `app/` directory.

### Build Instructions
1. Open the project in Android Studio (Ladybug or later recommended).
2. Set JDK to Java 17/21+.
3. Sync Gradle files.
4. Run `app` configuration on an emulator or physical device.
