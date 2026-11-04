package nl.designlama.pakkiepakkie.data

/**
 * Firestore collection paths and field names for reviews.
 */
object ReviewFirestoreFields {
    const val KENTEKEN = "kenteken"
    const val USER_ID = "userId"
    const val RATING = "rating"
    const val TEXT = "text"
    const val CREATED_AT_MILLIS = "createdAtMillis"
    const val UPDATED_AT_MILLIS = "updatedAtMillis"
    const val AVERAGE_RATING = "averageRating"
    const val REVIEW_COUNT = "reviewCount"
    const val RATING_SUM = "ratingSum"
    const val LATEST_REVIEWED_AT_MILLIS = "latestReviewedAtMillis"
    const val LATEST_TEXT = "latestText"
}
