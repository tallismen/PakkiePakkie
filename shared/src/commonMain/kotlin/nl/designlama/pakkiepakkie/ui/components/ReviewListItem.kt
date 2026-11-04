package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.designlama.pakkiepakkie.data.Review
import androidx.compose.ui.tooling.preview.PreviewLightDark
import org.jetbrains.compose.resources.stringResource
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.review_time_days_ago
import pakkiepakkie.shared.generated.resources.review_time_hours_ago
import pakkiepakkie.shared.generated.resources.review_time_just_now
import pakkiepakkie.shared.generated.resources.review_time_minutes_ago
import pakkiepakkie.shared.generated.resources.review_time_months_ago
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun ReviewListItem(
    review: Review,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StarRating(rating = review.rating, starSizeSp = 18f)
            Text(
                text = formatReviewTimestamp(review.updatedAtMillis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        review.text?.let { body ->
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun formatReviewTimestamp(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val now = Clock.System.now().toEpochMilliseconds()
    val deltaSeconds = ((now - epochMillis).coerceAtLeast(0L)) / 1000L
    val days = deltaSeconds / 86_400L
    val hours = (deltaSeconds % 86_400L) / 3_600L
    val minutes = (deltaSeconds % 3_600L) / 60L
    return when {
        days > 30 -> stringResource(Res.string.review_time_months_ago, (days / 30).toInt())
        days > 0 -> stringResource(Res.string.review_time_days_ago, days.toInt())
        hours > 0 -> stringResource(Res.string.review_time_hours_ago, hours.toInt())
        minutes > 0 -> stringResource(Res.string.review_time_minutes_ago, minutes.toInt())
        else -> stringResource(Res.string.review_time_just_now)
    }
}

@OptIn(ExperimentalTime::class)
@PreviewLightDark
@Composable
private fun ReviewListItemPreview() {
    val now = Clock.System.now().toEpochMilliseconds()
    PreviewContainer {
        ReviewListItem(
            review = Review(
                id = "PL700K_user1",
                kenteken = "PL700K",
                userId = "user1",
                rating = 4,
                text = "Leuke auto, trekt lekker door op de snelweg.",
                createdAtMillis = now - 3_600_000,
                updatedAtMillis = now - 3_600_000,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
