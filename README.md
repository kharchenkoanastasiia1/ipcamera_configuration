# IPCamera Configuration

A JavaFX desktop application for monitoring and synchronizing IP camera clocks against an NTP server. It loads camera and recorder information from an existing Firebird database, caches data in H2, and provides manual and scheduled time management through a graphical interface.

The application supports Axis cameras and a specific `ip_camera` API implementation. Support is determined by stream URL patterns and device APIs; it does not cover every IP camera model.

## Features

- Load camera inventory and recorder information from Firebird.
- Cache inventory, configuration, and per-camera flags in a local H2 database.
- Check camera reachability and optionally check RTSP connectivity.
- Retrieve firmware versions and camera timestamps using device-specific requests.
- Compare camera time with NTP time and display the difference in minutes.
- Update camera date, time, and time zone manually or automatically.
- Select camera categories for automatic updates and exclude individual cameras.
- Maintain a separate set of cameras for additional scheduled checks in “manual” mode.
- Search cameras by nickname and display associated lines and recorders.
- Display an RTSP video preview using JavaCV and FFmpeg.
- Refresh recorder connections associated with Axis cameras after time update requests.
- Write application logs to the console and rotating files, and send selected events to Seq.

The current graphical interface uses Ukrainian labels.

## Technology Stack

| Component | Technology |
| --- | --- |
| Language | Java 21 |
| Desktop UI | JavaFX 21, FXML |
| Build | Maven |
| Source database | Firebird, Jaybird JDBC |
| Local storage | H2 |
| Camera requests | Apache HttpClient, JSON, Jsoup |
| Reference time | NTP via Apache Commons Net |
| Video | JavaCV and FFmpeg |
| Logging | SLF4J, Logback, SerilogJ, Seq |
| Tests | JUnit and Mockito |

The POM currently uses JavaFX `21.0.10-ea+1` and includes FFmpeg native binaries for `windows-x86_64`.

## Requirements

- Windows x64 for the current native video dependency configuration.
- JDK 21 and Maven available on `PATH`.
- A desktop session capable of running JavaFX.
- Access to an existing Firebird database with the expected schema.
- Camera credentials with permission to read and change device time settings.
- Network access to cameras, recorders, and an NTP server.
- Write access to the application's working directory for H2 and logs.
- Docker with Compose only if using the supplied local Seq configuration.

The repository does not include a Firebird server or a script that creates the source inventory schema. The application expects an existing database.

## Project Structure

| Path / package | Purpose |
| --- | --- |
| `src/main/java/org/ipcamera/config/Application.java` | Launcher configured in Maven and the JAR manifest |
| `Main.java` | JavaFX application, Seq initialization, and single-instance lock |
| `*GuiController.java`, `IPCamerasGUIController.java` | UI actions and settings dialogs |
| `controller/` | Inventory loading, scheduled checks, time updates, and recorder connection refresh |
| `db/` | Firebird and H2 access |
| `entity/` | Camera, recorder, and configuration models |
| `factory/` | Camera classification and construction |
| `repository/` | Reachability, camera APIs, and recorder commands |
| `service/` | Time conversion, NTP, sorting, search helpers, and file operations |
| `constants/` | SQL, API requests, time zones, and UI constants |
| `src/main/resources/` | FXML layouts, database properties, and Logback configuration |
| `src/test/java/` | Unit tests |
| `docker-compose.yml` | Optional Seq container |

## Configuration

### Database Credentials and Local Storage

Edit `src/main/resources/db.properties` before building:

```properties
db.user=YOUR_FIREBIRD_USER
db.password=YOUR_FIREBIRD_PASSWORD
h2.url=jdbc\:h2\:./data/ip_cameras
h2.user=sa
h2.password=
```

Firebird uses `db.user` and `db.password`. The Firebird connection URL is stored separately in the application's configuration.

This properties file is loaded from the classpath and packaged into the JAR. Editing a standalone file beside the JAR will not override it; rebuild after changing these properties.

The default H2 URL creates or opens `data/ip_cameras.mv.db` relative to the working directory. H2 tables are created by the application when needed. The supplied archive already contains a local database, so existing saved settings may override constructor defaults.

### Application Settings

Open **Налаштування** (Settings) in the UI to configure the Firebird URL, NTP host, intervals, thresholds, worker count, and optional checks.

The database path refers to the file on the Firebird server. Replace the example with your server's actual connection details.

| Setting | Constructor Default | Purpose |
| --- | --- | --- |
| `threadCount` | `20` | Workers for regular checking and automatic updates |
| `intervalMinutes` | `30` | Delay between regular check/update cycles, in minutes |
| `delta` | `3` | Time difference threshold, in minutes |
| `checkRTSP` | `false` | Enables RTSP connectivity checks |
| `autoUpdateAxis` | `false` | Enables automatic updates for Axis cameras |
| `autoUpdateIPCamera` | `false` | Enables automatic updates for `ip_camera` devices |
| `autoUpdateManual` | `false` | Enables automatic updates for cameras marked as manual |
| `checkManual` | `false` | Enables the additional manual-mode checking schedule |
| `intervalMinutesManual` | `2` | Delay between additional manual-mode checks |
| `deltaManual` | `3` | Stored manual-mode threshold; see implementation notes below |

The constructor contains environment-specific Firebird and NTP addresses. Set appropriate values for your installation. Settings are persisted in H2; changing constructor defaults does not overwrite an existing configuration record.

### Seq Logging

Seq is initialized in `Main.main()` through SerilogJ. The server address and API key are currently hardcoded there, rather than loaded from the settings dialog.

Replace that configuration with your own values before building, for example:

```java
Log.setLogger(new LoggerConfiguration()
        .writeTo(seq("http://localhost:5341", "YOUR_SEQ_API_KEY"))
        .setMinimumLevel(LogEventLevel.Verbose)
        .createLogger());
```

The supplied source contains installation-specific credentials that should be replaced before sharing or deployment.

To run the optional local Seq container:

```powershell
docker compose up -d seq
```

The Compose file maps host port `5341` to container port `80`, so the local address is `http://localhost:5341`.

## Build and Run

Run commands from the project root so relative database and log paths remain consistent.

First, check your Java and Maven installations and build the JAR:

```powershell
java -version
mvn -version
mvn clean package
```

Maven Shade Plugin packages the application and its dependencies into:

```text
target/ipcamera_configuration-1.0-SNAPSHOT.jar
```

Next, use `jpackage` from your JDK to create the Windows executable with a bundled Java runtime:

```powershell
jpackage --input target --main-jar ipcamera_configuration-1.0-SNAPSHOT.jar --main-class org.ipcamera.config.Application --name TimeControl --type app-image --java-options "--enable-preview"
```

The executable is created at:

```text
TimeControl/TimeControl.exe
```

The `app-image` option creates a runnable application folder rather than an installer. Keep the entire `TimeControl` folder when moving or distributing the application.

Launch the application from its directory:

```powershell
Set-Location .\TimeControl
.\TimeControl.exe
```

Relative database and log paths are resolved against the working directory. To reuse an existing local database, copy the project's `data` folder into `TimeControl` before launching.

The application reserves TCP port `44555` as a single-instance lock. If the port is already occupied, startup returns without opening the UI.

## First Run

1. Configure Firebird credentials and the Seq endpoint before building.
2. Start the application from a writable working directory.
3. Open Settings and enter the Firebird URL and NTP host for your environment.
4. Confirm that the inventory loads and camera details are shown.
5. Check one camera and verify its reported time before enabling automatic updates.
6. Open **Параметри автооновлення** (Automatic Update Settings), select the required camera categories, and press **Пуск** (Start).

Regular time checks start automatically when the UI initializes. Automatic time updates are started with the Start button. **Стоп** (Stop) stops automatic updates and resumes regular checking.

## Using the Interface

| UI Action | Purpose |
| --- | --- |
| Select a camera | View IP address, lines, nicknames, recorder names, firmware, reachability, time difference, and check/update timestamps |
| **Перевірити час** (Check Time) | Check the selected camera against NTP |
| **Оновити час** (Update Time) | Request a time update for the selected camera |
| **Показати відео** (Show Video) | Open an RTSP preview |
| **Пошук** (Search) | Find cameras by nickname |
| **Параметри автооновлення** (Automatic Update Settings) | Choose categories eligible for scheduled time updates |
| **Налаштування** (Settings) | Change connection and monitoring settings; the dialog also provides inventory refresh |
| Manual-mode checkbox | Add or remove the selected camera from the additional checking set |
| Exclude-from-update checkbox | Prevent automatic time updates for the selected camera |

Manual mode means an additional scheduled checking set; it is separate from a one-time manual time update.

Status styling uses green for successful checks, orange for failed time retrieval, red for excessive time differences, and black for unreachable devices. Purple backgrounds mark manual-mode cameras; pink backgrounds mark cameras excluded from automatic updates.

## Scheduling and Time Updates

Regular checks and automatic updates each use a single scheduler thread and a configurable worker pool. Cycles run immediately, then wait `intervalMinutes` after the previous cycle completes. Additional manual-mode checks start after one minute, use five workers, and wait `intervalMinutesManual` between completed cycles.

Time differences are calculated in minutes. Automatic updates are eligible when the difference is **greater than or equal to** `delta`. A missing camera timestamp also causes the comparison method to return an update-needed result. Per-camera exclusions and enabled update categories are applied before automatic updates.

Time zone values are defined in `Constants.java`, including `Europe/Kyiv`, `Europe/Helsinki`, and the Axis POSIX time zone string. They are not freely selected in the settings dialog.

Device request implementations commonly retry up to three times. After Axis time update requests, associated recorder lines are refreshed through `LineUpdateController` and `LineRepository`.

## Persistence and Logs

| Location | Contents |
| --- | --- |
| `data/ip_cameras.mv.db` | H2 inventory cache, recorder data, configuration, and per-camera flags |
| `logs/ipcamera.log` | Current application log |
| `logs/ipcamera-YYYY-MM-DD.log` | Daily rotated logs; retention is 30 days |
| `critical_errors.txt` | Critical error history displayed by the UI when present |
| Seq | Events explicitly written through SerilogJ |

H2 tables are `ip_cameras`, `lines`, `configurations`, and `manual`. When Firebird retrieval fails, `DBController` attempts to use cached camera and recorder data from H2. A fresh local database has no inventory to fall back to.

Local Logback output and Seq events use separate logging paths; not every local log message is sent to Seq. The H2 cache contains device credentials, so handle database files as sensitive installation data.

## Testing

```powershell
mvn test
```

Tests cover database connectors, repositories, camera factories, URL and time conversion, sorting, and collection utilities. Network access, device behavior, JavaFX rendering, and native video playback require separate integration checks on the target system.

The POM declares `junit-jupiter-api:6.0.3`, but does not explicitly configure `junit-jupiter-engine` or a Maven Surefire version. Check test discovery and the results in `target/surefire-reports`; a successful build alone does not prove that every test ran.

## Troubleshooting

| Problem | What to Check |
| --- | --- |
| No window appears | Whether TCP port `44555` is already occupied |
| Build fails | JDK 21, Maven dependency access, and the pinned JavaFX version |
| Firebird connection fails | JDBC URL, server-side database path, credentials, network access, and schema |
| No cameras are listed | `FF_VIDEO` filter, supported URL patterns, and available H2 cache |
| Camera is marked unreachable | Network routing and Java `InetAddress.isReachable()` behavior; this is not identical to every OS ping implementation |
| Time cannot be read or updated | Device API compatibility, firmware branch, credentials, and NTP availability |
| Manual-mode threshold has no effect | Current checks use `delta`; `deltaManual` is stored but is not passed into the shared time comparison |
| Video preview fails | RTSP credentials, stream availability, and Windows x64 FFmpeg libraries |
| Events do not appear in local Seq | Hardcoded address/key in `Main.java` matching the Compose instance |
| Settings differ from source defaults | Previously saved settings in the H2 `configurations` table |

## Implementation Notes

- Source database creation and schema migration are outside the application.
- HTTP request compatibility depends on the supported camera API, not just its RTSP URL.
- The correction threshold uses `>= delta`, while red status styling uses `> delta`; behavior at the exact threshold can differ.
- `deltaManual` is configurable and persisted, but the shared checking path currently uses the regular `delta` value.
- NTP requests can fail with exceptions; the code does not provide a general fallback to PC time for camera synchronization.
- Bundled database files and logs are installation data, not clean application defaults.