package nl.designlama.pakkiepakkie.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import nl.designlama.pakkiepakkie.firebase.FirebaseAuthService
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate
import org.koin.core.annotation.Single

/**
 * Reads and writes license-plate reviews.
 *
 * Firestore plan (not implemented yet):
 * - Collection `reviews`
 * - Document id: `{kenteken}_{userId}` (enforces one review per user per plate)
 * - Fields: kenteken, userId, rating, text, createdAt, updatedAt
 */
@Single
class ReviewRepository(
    private val firebaseAuthService: FirebaseAuthService,
) {

    fun observeReviewsForKenteken(kenteken: String): Flow<List<Review>> {
        val norm = sanitizeLicensePlate(kenteken)
        if (norm.length != 6) return flowOf(emptyList())
        // TODO("Firestore: query reviews where kenteken == norm, order by updatedAt desc")
        return flowOf(emptyList())
    }

    fun observeLatestReviewedPlates(limit: Int = 20): Flow<List<LatestReviewedPlate>> {
        // TODO("Firestore: query latest reviewed plates ordered by latestReviewedAt desc, limit $limit")
        return flowOf(emptyList())
    }

    suspend fun getMyReviewForKenteken(kenteken: String): Review? {
        val norm = sanitizeLicensePlate(kenteken)
        if (norm.length != 6) return null
        // TODO("Firestore: ensureAnonymousAuth + get reviews/{kenteken}_{uid}")
        @Suppress("UNUSED_VARIABLE")
        val uid = firebaseAuthService.currentUserId
        return null
    }

    suspend fun upsertReview(
        kenteken: String,
        rating: Int,
        text: String?,
    ): Result<Review> {
        val norm = sanitizeLicensePlate(kenteken)
        if (norm.length != 6) {
            return Result.failure(IllegalArgumentException("Kenteken moet 6 tekens zijn"))
        }
        if (rating !in Review.MIN_RATING..Review.MAX_RATING) {
            return Result.failure(IllegalArgumentException("Beoordeling moet tussen 1 en 5 sterren zijn"))
        }
        val normalizedText = text?.trim()?.ifBlank { null }
        if (normalizedText != null && normalizedText.length > Review.MAX_TEXT_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Tekst mag maximaal ${Review.MAX_TEXT_LENGTH} tekens zijn"),
            )
        }
        // TODO("Firestore: ensureAnonymousAuth + set reviews/{kenteken}_{uid} with merge")
        return Result.failure(
            UnsupportedOperationException("Beoordelingen zijn nog niet beschikbaar"),
        )
    }
}
