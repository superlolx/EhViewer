# EhViewer

Fork of [FooIbar/EhViewer](https://github.com/FooIbar/EhViewer) — An unofficial E-Hentai application for Android. Built with Kotlin, Jetpack Compose, and Material Design 3.

## File structure

### Root config

- `build.gradle.kts`           — Root Gradle build script
- `settings.gradle.kts`        — Gradle settings (module includes)
- `gradle.properties`          — Gradle properties
- `gradle/libs.versions.toml`  — Version catalog
- `gradle/wrapper/`            — Gradle wrapper (JAR + properties)
- `gradlew` / `gradlew.bat`    — Gradle wrapper scripts
- `renovate.json`              — Renovate dependency bot config
- `rust-toolchain.toml`        — Rust toolchain config
- `.java-version`              — Java version config
- `.editorconfig`              — Editor config
- `.gitignore`                 — Git ignore rules
- `.github/`                   — GitHub Actions and templates

### app/ — Main application module

- `build.gradle.kts`           — App module build script
- `proguard-rules.pro`         — ProGuard/R8 rules
- `keystore/`                  — Signing keystore
- `src/main/kotlin/com/hippo/ehviewer/` — Main source code
  - `client/`                  — E-Hentai API client, data models, parsers
  - `coil/`                    — Coil image loading integration
  - `dailycheck/`              — Daily check-in feature
  - `download/`                — Download manager
  - `gallery/`                 — Gallery detail screens
  - `image/`                   — Image loading utilities
  - `jni/`                     — JNI native bindings
  - `ktbuilder/`               — Kotlin builder utilities
  - `ktor/`                    — Ktor HTTP client integration
  - `legacy/`                  — Legacy migration code
  - `shortcuts/`               — App shortcuts
  - `spider/`                  — Spider (reader) engine
  - **`ui/`**                  — **UI layer**
    - `legacy/`                — Legacy UI compatibility
    - `login/`                 — Login screen
    - `main/`                  — Main activity and navigation
    - `reader/`                — Gallery reader
    - `screen/`                — Feature screens (favorites, downloads, history, etc.)
    - `settings/`              — Settings screens
    - `theme/`                 — Material Design 3 theming
    - `tools/`                 — Shared UI components
  - `updater/`                 — App update checker
  - `util/`                    — General utilities
- `src/main/res/`              — Android resources (layouts, drawables, values, etc.)
- `src/main/rust/`             — Rust native code
- `src/main/cpp/`              — C++ native code
- `src/debug/`                 — Debug build config
- `src/marshmallow/`           — Android 6.0 compatibility

### core/ — Shared library modules (KMP)

- `core/common/`               — Common Kotlin code (shared across platforms)
- `core/data/`                 — Data layer (database, repositories, schemas)
- `core/ui/`                   — Shared UI components
- `core/i18n/`                 — Internationalization

### benchmark/ — Performance benchmarks

### build-logic/ — Convention Gradle plugins

- `convention/`                — Shared build conventions
- `settings.gradle.kts`        — Build-logic settings
- `gradle.properties`          — Build-logic properties

### build-apk/ — APK build scripts

### docs/ — Documentation

- `docs/README/`               — README translations (zh-CN, zh-TW, ja)

### Other

- `CHANGELOG.zh-CN.MD`        — Changelog (Chinese)
- `LICENSE`                    — License file
- `NOTICE`                     — Legal notices

## Dependencies

- **Build**: Gradle + Kotlin DSL, version catalog
- **Language**: Kotlin, Rust (native), C++ (native)
- **UI**: Jetpack Compose + Material Design 3
- **HTTP**: Ktor client
- **Images**: Coil
- **Database**: Room (SQLite)
- **Paging**: Paging Compose
- **Navigation**: Custom navigation (Destinations)
- **Background**: WorkManager
- **Native**: Rust via Mozilla's android-gradle-plugin, C++ via NDK

## Notes

- Cloned from https://github.com/FooIbar/EhViewer
- Active development branch: `main`
- Not submitted to Google Play — distributed via GitHub Releases
