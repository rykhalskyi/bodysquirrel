---
created: 2026-08-24
type: build-deploy
tags: [build, test, gradle, android]
related: [[project-overview]]
---

# Build & Deploy — Body Squirrel

## Prerequisites

- JDK (foojay resolver is configured in `settings.gradle.kts`; Java 11 source/target)
- Android SDK (compile/target SDK 37, min SDK 24)
- `local.properties` with `SILICON_FLOW_KEY` (optional; empty string if absent, injected into `BuildConfig.SILICON_FLOW_KEY`)

## Build

```bash
./gradlew assembleDebug      # build debug APK
./gradlew assembleRelease    # build release APK
```

## Test

```bash
./gradlew test               # unit tests (src/test)
./gradlew connectedAndroidTest # instrumented tests (src/androidTest, requires device/emulator)
```

Existing tests: `PortionCalculatorTest` (unit), `ExampleUnitTest`, `ExampleInstrumentedTest`.

## Lint / typecheck

No dedicated lint/typecheck task is configured beyond the Gradle build. Static analysis comes from the Kotlin/AGP compile. (Candidate: add `./gradlew lint` to CI.)

## Deploy

No CI, Play Store, or distribution config is present. Manual `assembleRelease` + install for now.

## Configuration notes

- Version catalog: `gradle/libs.versions.toml`.
- Room schema export disabled (`exportSchema = false` in `AppDatabase.kt`); KSP `room.schemaLocation` arg points at `$projectDir/schemas` (currently empty).
- Repositories mode `FAIL_ON_PROJECT_REPOS`; repos are `google()` + `mavenCentral()`.
