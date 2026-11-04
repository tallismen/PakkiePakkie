package nl.designlama.pakkiepakkie.resources

import org.jetbrains.compose.resources.getString
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.error_kenteken_invalid
import pakkiepakkie.shared.generated.resources.error_kenteken_length
import pakkiepakkie.shared.generated.resources.error_rating_range
import pakkiepakkie.shared.generated.resources.error_rating_required
import pakkiepakkie.shared.generated.resources.error_review_save_failed
import pakkiepakkie.shared.generated.resources.error_review_text_too_long
import pakkiepakkie.shared.generated.resources.error_vehicle_load_failed
import pakkiepakkie.shared.generated.resources.error_vehicle_not_found

/**
 * Suspend helpers for resolving user-facing strings outside Composables (ViewModels, repos).
 */
object StringResources {
    suspend fun kentekenMustBeSixChars(): String = getString(Res.string.error_kenteken_length)
    suspend fun kentekenInvalid(): String = getString(Res.string.error_kenteken_invalid)
    suspend fun vehicleLoadFailed(): String = getString(Res.string.error_vehicle_load_failed)
    suspend fun vehicleNotFound(kenteken: String): String =
        getString(Res.string.error_vehicle_not_found, kenteken)
    suspend fun ratingRange(): String = getString(Res.string.error_rating_range)
    suspend fun ratingRequired(): String = getString(Res.string.error_rating_required)
    suspend fun reviewTextTooLong(max: Int): String =
        getString(Res.string.error_review_text_too_long, max)
    suspend fun reviewSaveFailed(): String = getString(Res.string.error_review_save_failed)
}
