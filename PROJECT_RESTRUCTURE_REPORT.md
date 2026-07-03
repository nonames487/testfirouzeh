# FIROOZEH Workspace Restructure Report

Generated: 2026-07-02

## Summary

The workspace was reorganized into a clean top-level layout without permanently deleting files.

```text
/android   Android application project root
/backend   FastAPI backend project root
/docs      context/export/documentation files
/scripts   maintenance/bootstrap scripts
/archive   backups, duplicate sources, legacy trees, and zip archives
```

## Detected Project Roots

- Android project root: `android`
- Android module: `android/app`
- Backend FastAPI root: `backend`
- Backend entrypoint: `backend/app/main.py`

## Backup

A full pre-restructure backup was created before moving files:

- `archive/pre_restructure_backup_20260702_081945`

## File Moves

### Android Sources

Root-level Java files with package declarations were detected as misplaced Android source files. Existing newer/canonical copies under `android/app/src/main/java` were preserved. Duplicate root copies were moved to:

- `archive/misplaced_android_sources`

The following unique Android source was moved into the Android module:

- `AtomicIdGenerator.java` -> `android/app/src/main/java/ai/arena/hisabdar/modern1405/util/AtomicIdGenerator.java`

Duplicate root sources archived:

- `AccountingDao.java`
- `AccountingRepository.java`
- `AppDatabase.java`
- `AppHealthMonitor.java`
- `BazaarCreditScoringEngine.java`
- `BazaarNotificationUtil.java`
- `ConsultingRoomFragment.java`
- `HomeFragment.java`
- `IdGenerator.java`
- `InvoicePdfUtil.java`
- `LocalRagVectorDatabase.java`
- `MainActivity.java`
- `MainViewModel.java`
- `NewsAcademyFragment.java`
- `OcrInvoiceProcessor.java`
- `Party.java`
- `PriceManagerFragment.java`
- `Product.java`
- `QrCodeAuthScanner.java`
- `SemanticSearchFragment.java`
- `SettingsGuideFragment.java`
- `SmsPriceListener.java`
- `SolvencyDashboardFragment.java`
- `SyncQueue.java`

### Gradle Duplicates

The real Gradle project root is `android`, so root-level duplicate Gradle files were archived to:

- `archive/invalid_gradle_duplicates`

Archived files:

- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `build.gradle (۱).kts`

### Docs

Context/export files were moved to `docs`:

- `extracted_context.txt` -> `docs/extracted_context.txt`
- `firoozeh_optimized_context_v10.txt` -> `docs/firoozeh_optimized_context_v10.txt`
- `تمام پروژه فیروزه تا نسخه ۱۰.۰.۲.md` -> `docs/تمام پروژه فیروزه تا نسخه ۱۰.۰.۲.md`

### Archives

Zip/backup files were moved to `archive/zip_backups`:

- `Hisabdar_Firoozeh_v10.0.7_Source_and_Backend.zip`
- `android.zip`

The empty/legacy root `app` tree was moved to:

- `archive/legacy_app_tree`

## Android Fixes Applied

- Kept Room database structure intact in `android/app/src/main/java/ai/arena/hisabdar/modern1405/db/AppDatabase.java`.
- Preserved MVVM structure under `repository`, `ui`, `util`, `viewmodel`, and `db` packages.
- Preserved FastAPI backend files under `backend`.
- Added FileProvider manifest registration for PDF sharing.
- Added `android/app/src/main/res/xml/file_paths.xml` for cache-file sharing.
- Added missing Gradle bootstrap files:
  - `android/gradlew`
  - `android/gradlew.bat`
  - `android/gradle/wrapper/gradle-wrapper.properties`
- Added `scripts/bootstrap_gradle_wrapper.sh` to regenerate a standard Gradle wrapper when system Gradle is available.
- Added `android/local.properties` placeholder. No SDK path was detected in this environment, so `sdk.dir` was not set.

## Static Validation

- Package-directory mismatches: none detected under `android/app/src/main/java`.
- Internal import mismatches: none detected by static scan.
- Resource ID/layout mismatches: no active blockers detected. `R.id.bottom_navigation` appears only in commented code.

## Build Attempt

Command attempted:

```bash
cd android
./gradlew assembleDebug
```

Result:

```text
ERROR: Java/JDK is required to run Gradle, but java was not found on PATH.
```

The build did not reach dependency sync or Android compilation because this environment has no `java`, no system `gradle`, and no detectable Android SDK.

## APK Path

No APK was generated in this environment.

Expected APK path after installing prerequisites and running a successful build:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

## Required Manual Fixes

Install/configure local build prerequisites, then rerun the build:

1. Install JDK 17 and ensure `java` is on `PATH`.
2. Install Android SDK / Android Studio.
3. Set `sdk.dir` in `android/local.properties`, for example:

```properties
sdk.dir=C\:\\Users\\<USER>\\AppData\\Local\\Android\\Sdk
```

or on Linux/WSL:

```properties
sdk.dir=/path/to/Android/Sdk
```

4. From `android`, run:

```bash
./gradlew assembleDebug
```

If the bootstrap wrapper cannot download Gradle, install system Gradle and run:

```bash
cd android
gradle wrapper --gradle-version 8.0 --distribution-type bin
./gradlew assembleDebug
```

## Notes

- No files were permanently deleted.
- All removed-from-root files were moved into `archive` or `docs`.
- Backend API files were preserved unchanged.
- Room migrations and database versioning were not modified.
