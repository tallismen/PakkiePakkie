package nl.designlama.pakkiepakkie.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import nl.designlama.pakkiepakkie.firebase.FirebaseAuthService
import nl.designlama.pakkiepakkie.resources.StringResources
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate
import nl.designlama.pakkiepakkie.utils.LogTag
import nl.designlama.pakkiepakkie.utils.Logger
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Reads and writes license-plate reviews in Firestore.
 *
 * Collections:
 * - `reviews` — document id `{kenteken}_{userId}` (one review per user per plate)
 * - `latest_reviewed_plates` — denormalized home-feed aggregates, updated on upsert
 */
@OptIn(ExperimentalTime::class)
@Single
class ReviewRepository(
    private val firebaseAuthService: FirebaseAuthService,
) {

    private val db get() = Firebase.firestore

    fun observeReviewsForKenteken(kenteken: String): Flow<List<Review>> {
        val norm = sanitizeLicensePlate(kenteken)
        if (norm.length != 6) return flowOf(emptyList())
        return flow {
            firebaseAuthService.ensureAnonymousAuth()
            emitAll(
                db.collection(ReviewFirestorePaths.REVIEWS)
                    .where { ReviewFirestoreFields.KENTEKEN equalTo norm }
                    .orderBy(ReviewFirestoreFields.UPDATED_AT_MILLIS, Direction.DESCENDING)
                    .snapshots
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            runCatching { doc.data<ReviewFirestoreDto>().toReview(doc.id) }.getOrNull()
                        }
                    },
            )
        }.catch { error ->
            Logger.v(LogTag.DEFAULT, "Failed to observe reviews for $norm: ${error.message}")
            emit(emptyList())
        }
    }

    fun observeLatestReviewedPlates(limit: Int = 20): Flow<List<LatestReviewedPlate>> {
        val safeLimit = limit.coerceIn(1, 50)
        return flow {
            firebaseAuthService.ensureAnonymousAuth()
            emitAll(
                db.collection(ReviewFirestorePaths.LATEST_REVIEWED_PLATES)
                    .orderBy(ReviewFirestoreFields.LATEST_REVIEWED_AT_MILLIS, Direction.DESCENDING)
                    .limit(safeLimit)
                    .snapshots
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            runCatching { doc.data<LatestReviewedPlateFirestoreDto>().toDomain() }.getOrNull()
                        }
                    },
            )
        }.catch { error ->
            Logger.v(LogTag.DEFAULT, "Failed to observe latest reviewed plates: ${error.message}")
            emit(emptyList())
        }
    }

    suspend fun getMyReviewForKenteken(kenteken: String): Review? {
        val norm = sanitizeLicensePlate(kenteken)
        if (norm.length != 6) return null
        return runCatching {
            val uid = firebaseAuthService.ensureAnonymousAuth()
            val docId = ReviewFirestorePaths.reviewDocumentId(norm, uid)
            val snapshot = db.collection(ReviewFirestorePaths.REVIEWS).document(docId).get()
            if (!snapshot.exists) {
                null
            } else {
                snapshot.data<ReviewFirestoreDto>().toReview(docId)
            }
        }.onFailure { error ->
            Logger.v(LogTag.DEFAULT, "Failed to load my review for $norm: ${error.message}")
        }.getOrNull()
    }

    suspend fun upsertReview(
        kenteken: String,
        rating: Int,
        text: String?,
    ): Result<Review> {
        val norm = sanitizeLicensePlate(kenteken)
        if (norm.length != 6) {
            return Result.failure(IllegalArgumentException(StringResources.kentekenMustBeSixChars()))
        }
        if (rating !in Review.MIN_RATING..Review.MAX_RATING) {
            return Result.failure(IllegalArgumentException(StringResources.ratingRange()))
        }
        val normalizedText = text?.trim()?.ifBlank { null }
        if (normalizedText != null && normalizedText.length > Review.MAX_TEXT_LENGTH) {
            return Result.failure(
                IllegalArgumentException(StringResources.reviewTextTooLong(Review.MAX_TEXT_LENGTH)),
            )
        }

        return runCatching {
            val uid = firebaseAuthService.ensureAnonymousAuth()
            val now = Clock.System.now().toEpochMilliseconds()
            val reviewRef = db.collection(ReviewFirestorePaths.REVIEWS)
                .document(ReviewFirestorePaths.reviewDocumentId(norm, uid))
            val plateRef = db.collection(ReviewFirestorePaths.LATEST_REVIEWED_PLATES).document(norm)

            lateinit var saved: ReviewFirestoreDto
            db.runTransaction {
                val existingSnap = get(reviewRef)
                val existing = if (existingSnap.exists) {
                    existingSnap.data<ReviewFirestoreDto>()
                } else {
                    null
                }
                val createdAt = existing?.createdAtMillis ?: now
                saved = ReviewFirestoreDto(
                    kenteken = norm,
                    userId = uid,
                    rating = rating,
                    text = normalizedText,
                    createdAtMillis = createdAt,
                    updatedAtMillis = now,
                )
                // Full replace so clearing `text` actually removes the field.
                set(reviewRef, saved)

                val plateSnap = get(plateRef)
                val previous = if (plateSnap.exists) {
                    plateSnap.data<LatestReviewedPlateFirestoreDto>()
                } else {
                    null
                }
                val oldRating = existing?.rating
                val oldCount = previous?.reviewCount ?: 0
                val oldSum = previous?.ratingSum ?: 0
                val newCount = if (oldRating == null) oldCount + 1 else oldCount.coerceAtLeast(1)
                val newSum = if (oldRating == null) {
                    oldSum + rating
                } else {
                    oldSum - oldRating + rating
                }
                val average = if (newCount > 0) newSum.toDouble() / newCount.toDouble() else rating.toDouble()
                val plate = LatestReviewedPlateFirestoreDto(
                    kenteken = norm,
                    averageRating = average,
                    reviewCount = newCount,
                    ratingSum = newSum,
                    latestReviewedAtMillis = now,
                    latestText = normalizedText ?: previous?.latestText,
                )
                set(plateRef, plate)
            }
            saved.toReview(ReviewFirestorePaths.reviewDocumentId(norm, uid))
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Logger.v(LogTag.DEFAULT, "Failed to upsert review for $norm: ${error.message}")
                Result.failure(error)
            },
        )
    }
}
