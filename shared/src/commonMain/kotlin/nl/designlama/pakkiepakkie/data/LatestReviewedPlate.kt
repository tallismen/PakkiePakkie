package nl.designlama.pakkiepakkie.data

/**
 * Aggregate for the home “Recent beoordeeld” feed.
 *
 * Firestore: denormalized `latest_reviewed_plates/{kenteken}`, updated in the
 * same transaction as review upserts.
 */
data class LatestReviewedPlate(
    val kenteken: String,
    val averageRating: Float,
    val reviewCount: Int,
    val latestReviewedAtMillis: Long,
    val latestText: String? = null,
)
