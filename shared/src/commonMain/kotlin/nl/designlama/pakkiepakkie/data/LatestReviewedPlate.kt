package nl.designlama.pakkiepakkie.data

/**
 * Aggregate for the home “Recent beoordeeld” feed.
 *
 * Intended Firestore source: either a derived query over `reviews` or a
 * denormalized `latest_reviewed_plates` collection updated on upsert.
 */
data class LatestReviewedPlate(
    val kenteken: String,
    val averageRating: Float,
    val reviewCount: Int,
    val latestReviewedAtMillis: Long,
    val latestText: String? = null,
)
