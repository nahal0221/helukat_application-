package com.yourcompany.fieldtech.sync

/**
 * DISABLED SCAFFOLD — push notifications need a real Firebase project first.
 *
 * To re-enable:
 *  1. Add google-services.json to app/ from your Firebase project.
 *  2. Uncomment the google-services plugin + firebase-messaging dependency
 *     in app/build.gradle.kts.
 *  3. Uncomment the AndroidManifest.xml <service> entry for this class.
 *  4. Replace this class with:
 *
 *     class FieldTechFirebaseMessagingService : FirebaseMessagingService() {
 *         override fun onMessageReceived(message: RemoteMessage) {
 *             // Per the spec: push only signals a change — the app should
 *             // then call GET /notifications to refresh, not read job data
 *             // out of the push payload itself.
 *         }
 *         override fun onNewToken(token: String) {
 *             // POST /auth/register-device with the new token.
 *         }
 *     }
 */
object FieldTechFirebaseMessagingServicePlaceholder
