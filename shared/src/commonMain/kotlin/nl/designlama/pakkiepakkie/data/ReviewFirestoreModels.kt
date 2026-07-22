package nl.designlama.pakkiepakkie.data

import kotlinx.serialization.Serializable

@Serializable
internal data class ReviewFirestoreDto(
    val kenteken: String,
    val userId: String,
    val rating: Int,
    val text: String? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    fun toReview(documentId: String): Review = Review(
        id = documentId,
        kenteken = kenteken,
        userId = userId,
        rating = rating,
        text = text,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

/**
 * Denormalized plate summary for the home “Recent beoordeeld” feed.
 * Document id = kenteken. Updated in the same transaction as review upserts.
 */
@Serializable
internal data class LatestReviewedPlateFirestoreDto(
    val kenteken: String,
    val averageRating: Double,
    val reviewCount: Int,
    val ratingSum: Int,
    val latestReviewedAtMillis: Long,
    val latestText: String? = null,
) {
    fun toDomain(): LatestReviewedPlate = LatestReviewedPlate(
        kenteken = kenteken,
        averageRating = averageRating.toFloat(),
        reviewCount = reviewCount,
        latestReviewedAtMillis = latestReviewedAtMillis,
        latestText = latestText,
    )
}

internal object ReviewFirestorePaths {
    const val REVIEWS = "reviews"
    const val LATEST_REVIEWED_PLATES = "latest_reviewed_plates"

    fun reviewDocumentId(kenteken: String, userId: String): String = "${kenteken}_$userId"
}
