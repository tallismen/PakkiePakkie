package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.designlama.pakkiepakkie.data.Review
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun StarRating(
    rating: Int,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    onRatingChange: (Int) -> Unit = {},
    starSizeSp: Float = 22f,
) {
    val clamped = rating.coerceIn(0, Review.MAX_RATING)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        for (star in Review.MIN_RATING..Review.MAX_RATING) {
            val filled = star <= clamped
            Text(
                text = if (filled) "★" else "☆",
                fontSize = starSizeSp.sp,
                color = if (filled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = if (interactive) {
                    Modifier
                        .clickable(role = Role.Button) { onRatingChange(star) }
                        .padding(horizontal = 2.dp)
                } else {
                    Modifier
                },
            )
        }
    }
}

@Preview
@Composable
private fun StarRatingReadOnlyPreview() {
    PreviewContainer(isDarkTheme = false) {
        StarRating(rating = 4, modifier = Modifier.padding(16.dp))
    }
}

@Preview
@Composable
private fun StarRatingInteractivePreview() {
    PreviewContainer(isDarkTheme = false) {
        StarRating(
            rating = 3,
            interactive = true,
            starSizeSp = 28f,
            modifier = Modifier.padding(16.dp),
        )
    }
}
