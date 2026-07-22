package nl.designlama.pakkiepakkie.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.koin.core.annotation.Single

/**
 * Shared Firebase Auth helpers.
 *
 * Console checklist for project `pakkiepakkie-d3336`:
 * - Enable Anonymous sign-in under Authentication
 * - Create a Firestore database before using reviews
 * - Prefer registering dedicated Android apps for `.dev` / `.stag`
 *   applicationIds instead of sharing the prod client entry
 */
@Single
class FirebaseAuthService {

    val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    /**
     * Ensures an anonymous Firebase user exists and returns its uid.
     * Safe to call on every app start or before Firestore writes.
     */
    suspend fun ensureAnonymousAuth(): String {
        val existing = Firebase.auth.currentUser
        if (existing != null) return existing.uid
        val result = Firebase.auth.signInAnonymously()
        return result.user?.uid
            ?: error("Anonymous sign-in succeeded but no user was returned")
    }
}
