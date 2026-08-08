# Field Tech Android App — Starter Scaffold

## Get an installable APK on your phone (no Android Studio needed)
This repo includes `.github/workflows/build-apk.yml`, which builds a debug APK on
GitHub's servers (they have the Android SDK; this sandbox doesn't).

1. Create a new **public or private** GitHub repo and push this folder's contents to it.
2. On GitHub, go to the **Actions** tab → you should see "Build Debug APK" run
   automatically (or click **Run workflow** to trigger it manually).
3. When it finishes (green check, a few minutes), open the run → under **Artifacts**,
   download `FieldTechApp-debug` (a zip containing `app-debug.apk`).
4. On your phone: unzip it (or download directly if browsing GitHub from the phone),
   tap the `.apk` file, and allow "install from unknown sources" when prompted.
5. It'll open to a login screen — it won't actually log in successfully until
   `BASE_URL` in `app/build.gradle.kts` points at a real, running backend that
   implements this API spec. Right now there's no backend behind it.

Push notifications (Firebase) are **disabled** in this build so CI succeeds without
your own Firebase credentials — see "Push notifications" below to re-enable.


Generated from `api_endpoints_3.pdf` (Field Service & Operations Management API).
Targets the **Field Technician** app per the spec's Client-to-Endpoint Mapping Summary:
§4 Jobs (read own), §5 Field Activity, §6 Materials (read + usage logging),
§7 Notifications, `/sync/batch` for offline queue.

## Stack
Kotlin · Jetpack Compose · Hilt · Retrofit/Moshi · Room · WorkManager · DataStore · FCM

## Open in Android Studio
1. `File > Open` and select the `FieldTechApp` folder.
2. Let Gradle sync (it will download the AGP/Kotlin/Hilt/Compose versions pinned in
   `build.gradle.kts`).
3. **Push notifications**: disabled by default. To enable, create a Firebase project,
   download `google-services.json` into `app/`, then uncomment: the `google-services`
   plugin in root `build.gradle.kts`, the plugin + Firebase dependency in
   `app/build.gradle.kts`, the `<service>` entry in `AndroidManifest.xml`, and replace
   `FieldTechFirebaseMessagingService.kt` per the instructions in that file.
4. Update `BASE_URL` / `WS_URL` in `app/build.gradle.kts` if you're pointing at staging.

## What's implemented
- **Auth**: login, JWT storage (DataStore), silent refresh via an `okhttp3.Authenticator`.
- **Jobs**: list + detail, Room-cached for offline viewing, status-only PATCH (per FT's
  limited role scope).
- **Field Activity**: GPS-stamped time logs, status notes, material usage — all write to
  a local Room queue first, then flush via `POST /sync/batch` through a WorkManager
  `SyncWorker`, with an `Idempotency-Key` derived from the pending batch so retries on
  flaky connections can't double-create records.
- **Sync UI signal**: job list shows a pending-sync count badge.
- **Push scaffolding**: `FirebaseMessagingService` stub — per the spec, push should
  *trigger* a refresh of `GET /notifications`, not carry job data itself.

## What's intentionally left as TODOs (next steps)
1. **Location permissions**: `JobDetailScreen` stubs GPS coordinates. Wire up
   `FusedLocationProviderClient` + a runtime permission request flow before shipping —
   time logs are meaningless without real coordinates.
2. **Photo capture + upload**: entities and the `uploadPhoto` multipart endpoint exist;
   need a CameraX capture screen and local file storage before queuing into
   `photo_queue`.
3. **Signature capture**: `POST /jobs/{id}/signatures` is wired in `ApiService` but has
   no UI yet — needs a signature-pad Compose component.
4. **Checklist completion**: `PUT /jobs/{id}/checklist/{itemId}` is wired but the UI is
   read-only right now; add a tap-to-complete interaction.
5. **WebSocket** (`wss://.../v1/ws`): not implemented. Only relevant if you want live
   `job.assigned` / `job.updated` push-adjacent updates on top of FCM; OkHttp's
   `WebSocket` client is a natural fit alongside the existing OkHttpClient.
6. **FCM token registration**: `onNewToken` in the messaging service is a TODO — needs
   to call `POST /auth/register-device` through a Hilt-injected, background-safe path.
7. **Error/session handling**: no global "refresh token expired → route to login" flow
   yet; currently a failed silent refresh just leaves requests unauthenticated.
8. **Idempotency-Key strategy**: current implementation derives a stable key from the
   sorted set of pending local IDs. Confirm with your backend team this matches their
   dedup semantics before relying on it in production.

## Architecture at a glance
```
ui/            Compose screens + ViewModels (Login, JobList, JobDetail)
data/remote/   Retrofit ApiService, DTOs, auth interceptor/authenticator
data/local/    Room entities/DAO — the offline write queue + job cache
data/repository/  Bridges UI <-> network/local, offline-first write pattern
sync/          SyncWorker (flushes queue to /sync/batch), SyncScheduler, FCM service
di/            Hilt modules (network, database)
```
