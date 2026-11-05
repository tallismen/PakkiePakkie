package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.designlama.pakkiepakkie.theme.GaugeHigh
import nl.designlama.pakkiepakkie.theme.GaugeLow
import nl.designlama.pakkiepakkie.theme.GaugeMid
import kotlin.math.abs

/**
 * Visual outcome of a left-vs-right comparison for the battle bar.
 * Fill always grows from the left and represents the left side's share of (left+right).
 */
enum class CompareBarTone {
    /** Clear left win — green fill. */
    WinStrong,
    /** Narrow left win — orange fill. */
    WinClose,
    /** Clear left loss — red fill. */
    LoseStrong,
    /** Narrow left loss — short red/orange fill. */
    LoseClose,
    /** Equal / N/A — full grey. */
    Tie,
}

data class CompareOutcome(
    val leftTone: CompareBarTone,
    /** Left share of the total, 0..1. For ties use 1f (full grey track). */
    val leftShare: Float,
    val leftWins: Boolean,
    val isTie: Boolean,
)

/**
 * @param higherIsBetter true for power/speed; false for weight/time.
 * @param marginalRatio max(better,worse)/min within this → orange instead of green/red.
 */
fun compareNumericOutcome(
    left: Double?,
    right: Double?,
    higherIsBetter: Boolean,
    marginalRatio: Double = 1.12,
): CompareOutcome {
    if (left == null || right == null) {
        return CompareOutcome(CompareBarTone.Tie, 1f, leftWins = false, isTie = true)
    }
    val total = left + right
    if (total <= 0.0) {
        return CompareOutcome(CompareBarTone.Tie, 1f, leftWins = false, isTie = true)
    }
    val share = (left / total).toFloat().coerceIn(0.05f, 0.95f)
    if (abs(left - right) < 1e-6) {
        return CompareOutcome(CompareBarTone.Tie, 1f, leftWins = false, isTie = true)
    }
    val leftBetter = if (higherIsBetter) left > right else left < right
    val better = if (leftBetter) left else right
    val worse = if (leftBetter) right else left
    val close = worse > 0.0 && (better / worse) <= marginalRatio
    val tone = when {
        leftBetter && close -> CompareBarTone.WinClose
        leftBetter -> CompareBarTone.WinStrong
        close -> CompareBarTone.LoseClose
        else -> CompareBarTone.LoseStrong
    }
    return CompareOutcome(tone, share, leftWins = leftBetter, isTie = false)
}

fun compareBooleanOutcome(leftWins: Boolean, tie: Boolean): CompareOutcome = when {
    tie -> CompareOutcome(CompareBarTone.Tie, 1f, leftWins = false, isTie = true)
    leftWins -> CompareOutcome(CompareBarTone.WinStrong, 1f, leftWins = true, isTie = false)
    else -> CompareOutcome(CompareBarTone.LoseStrong, 0.2f, leftWins = false, isTie = false)
}

@Composable
fun ComparisonBar(
    leftShare: Float,
    tone: CompareBarTone,
    modifier: Modifier = Modifier,
) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    val fill = when (tone) {
        CompareBarTone.WinStrong -> GaugeHigh
        CompareBarTone.WinClose -> GaugeMid
        CompareBarTone.LoseStrong -> GaugeLow
        CompareBarTone.LoseClose -> GaugeLow.copy(alpha = 0.85f)
        CompareBarTone.Tie -> MaterialTheme.colorScheme.outlineVariant
    }
    val widthFraction = when (tone) {
        CompareBarTone.Tie -> 1f
        else -> leftShare.coerceIn(0.05f, 0.95f)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(track),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(widthFraction)
                .background(fill),
        )
    }
}

@Composable
fun CompareStatRow(
    label: String,
    leftValue: String,
    rightValue: String,
    outcome: CompareOutcome,
    modifier: Modifier = Modifier,
    icon: String? = null,
) {
    val defaultColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val winColor = GaugeHigh
    val leftColor = when {
        outcome.isTie -> mutedColor
        outcome.leftWins -> winColor
        else -> defaultColor
    }
    val rightColor = when {
        outcome.isTie -> mutedColor
        !outcome.leftWins -> winColor
        else -> defaultColor
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = leftValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = leftColor,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.weight(1.2f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.labelMedium,
                        color = mutedColor,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = mutedColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = rightValue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = rightColor,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ComparisonBar(leftShare = outcome.leftShare, tone = outcome.leftTone)
    }
}

@Composable
fun FormRow(
    icon: String,
    label: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}
