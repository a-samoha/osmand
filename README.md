# OsmAnd Map Downloader - Test Task (KMP)

A cross-platform mobile application for downloading offline maps, built with **Kotlin Multiplatform (KMP)** for Android and iOS. The project parses a complex hierarchical XML map structure, manages a robust sequential download queue, and displays data according to the strict Figma prototype guidelines.

## 🚀 Key Features Implemented

- **Hierarchical Navigation**: Dynamically builds the UI tree from a local XML structure. Tapping on split countries (e.g., Germany, France) opens a nested regions screen, while direct countries (e.g., Albania) can be downloaded instantly.
- **Sequential Download Queue**: Uses a thread-safe `Mutex` mechanism with limited parallelism to download maps strictly one after another (Single Thread/Queue behavior).
- **Network Resilience & Auto-Cleanup**: Wraps stream channels into robust try-catch blocks. If the connection drops mid-download, it automatically fires an MVI effect for a UI Toast and physically deletes the corrupted partial file from disk storage.
- **Ktorfit 3.x Streaming**: Leverages `@Streaming` and `kotlinx-io` chunked byte transfer to safely download massive map files without causing `OutOfMemory` errors.
- **Exact UI Specifications**: Adheres 100% to design rules: strictly 52dp row heights, hex color themes matching the palette (`#ff8800`, `#eaeaea`, `#212121`), custom 1dp separators, and reactive green icons on successful downloads.

---

## 🏗 Architecture & Design Patterns

The project is designed on top of **Clean Architecture** principles mixed with a unidirectional data flow (**MVI Pattern**) to ensure maximum scalability, complete platform decoupling, and clean state management.


- **Data Layer**: Handles low-level network fetching (Ktorfit), C-pointer iOS/Android platform network monitoring, and multiplatform file system access via `kotlinx-io`.
- **Domain Layer**: Houses business models (`RegionNode`) and the centralized `MapDownloadManager`, which acts as the single source of truth for the active queue and background disk scanning.
- **Presentation Layer**: Implements MVI state machines. ViewModels handle user `Intents`, update immutable `States`, and push asynchronous one-time `Effects` (e.g., ShowToast, Navigate).

---

## 🛠 Tech Stack & Libraries Used

- **Kotlin Multiplatform (KMP)** - Sharing 100% of business logic, network layers, and file management across Android and iOS.
- **Compose Multiplatform** - Declarative single-line native rendering for both platforms.
- **Koin** - High-utility dependency injection engine configured with clean platform-specific expectation module scopes (`singleOf`, `viewModelOf`).
- **Ktorfit 3.x & Ktor Client** - HTTP REST framework using KSP code-generation to emulate Retrofit аnnotations with full automatic `302 Redirect` tracking.
- **Kotlinx Serialization & XMLUtil 0.90+** - Advanced recursive XML serialization setup mapping multi-nested nodes and filtering raw attributes smoothly.
- **Kotlinx IO & SystemFileSystem** - Native Multiplatform File I/O engine to safely target app-isolated directory bundles without platform `expect/actual` rewrites.
- **SavedStateHandle** - Official safe-args navigation state preservation to filter inner country IDs cleanly upon deep process re-compositions.

---

## 📂 Naming Rules & Logic Mapping

As specified by the OsmAnd schema:
1. Every parsed raw map string is capitalized on its first character.
2. The final target endpoint string is appended with a hardcoded `_2.obf.zip` suffix rule.
  - *Example:* `denmark_europe` ➡️ `Denmark_europe_2.obf.zip`
3. Map availability is parsed strictly matching line 24 instructions (`map="yes"` / `map="no"` node values).


# This is a KMP project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
