package nl.designlama.pakkiepakkie.data

/**
 * A user review for a license plate.
 *
 * Firestore: collection `reviews`, document id `{kenteken}_{userId}`
 * so each user can have at most one review per plate.
 */
data class Review(
    val id: String,
    val kenteken: String,
    val userId: String,
    val rating: Int,
    val text: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
        const val MAX_TEXT_LENGTH = 160
    }
}
