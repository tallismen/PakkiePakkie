package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import nl.designlama.pakkiepakkie.theme.winChanceColor
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val ARC_START_ANGLE = 135f
private const val ARC_SWEEP_MAX = 270f

@Composable
fun PakkiePakkieGauge(
    percent: Float?,
    modifier: Modifier = Modifier,
    sizeDp: Float = 120f,
    showLabel: Boolean = true,
    animationEnabled: Boolean = true,
) {
    val darkTheme = isSystemInDarkTheme()
    val track = MaterialTheme.colorScheme.surfaceVariant
    val brand = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val targetPercent = percent?.coerceIn(0f, 100f)
    val targetKey = targetPercent?.roundToInt()
    val animatable = remember { Animatable(0f) }
    var lastAnimatedKey by remember { mutableIntStateOf(Int.MIN_VALUE) }

    LaunchedEffect(targetKey, animationEnabled) {
        if (targetKey == null) {
            animatable.snapTo(0f)
            lastAnimatedKey = Int.MIN_VALUE
            return@LaunchedEffect
        }
        val target = targetKey.toFloat()
        if (!animationEnabled) {
            animatable.snapTo(target)
            lastAnimatedKey = targetKey
            return@LaunchedEffect
        }
        if (lastAnimatedKey == targetKey && abs(animatable.value - target) < 0.5f) {
            return@LaunchedEffect
        }
        if (lastAnimatedKey == Int.MIN_VALUE) {
            animatable.snapTo(target)
            lastAnimatedKey = targetKey
            return@LaunchedEffect
        }
        if (abs(animatable.value - target) < 0.5f) {
            lastAnimatedKey = targetKey
            return@LaunchedEffect
        }
        animatable.animateTo(
            targetValue = target,
            animationSpec = PakkieAnimations.tweenSpec(),
        )
        lastAnimatedKey = targetKey
    }

    val animatedValue = if (targetPercent != null) animatable.value else null
    val progressColor = animatedValue?.let { winChanceColor(it, darkTheme) } ?: brand
    val sweep = ((animatedValue ?: 0f) / 100f) * ARC_SWEEP_MAX
    val dim = sizeDp.dp

    Box(modifier = modifier.size(dim), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(dim)) {
            val stroke = size.minDimension * 0.08f
            val pad = stroke / 2f + size.minDimension * 0.04f
            val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
            val topLeft = Offset(pad, pad)
            drawArc(
                color = track,
                startAngle = ARC_START_ANGLE,
                sweepAngle = ARC_SWEEP_MAX,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (animatedValue != null) {
                drawArc(
                    color = progressColor,
                    startAngle = ARC_START_ANGLE,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showLabel) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = brand,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        ) {
                            append("PakkiePakkie")
                        }
                        withStyle(
                            SpanStyle(
                                color = brand,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                        ) {
                            append("™")
                        }
                    },
                )
            }
            Text(
                text = animatedValue?.let { "${it.roundToInt()}%" } ?: "—",
                color = if (animatedValue != null) progressColor else onSurface,
                style = if (showLabel) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.labelLarge
                },
            )
        }
    }
}

@Composable
private fun PreviewContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PakkiePakkieGauge(percent = null, sizeDp = 100f)
        Spacer(modifier = Modifier.height(16.dp))
        PakkiePakkieGauge(percent = 15f, sizeDp = 100f)
        Spacer(modifier = Modifier.height(16.dp))
        PakkiePakkieGauge(percent = 50f, sizeDp = 100f)
        Spacer(modifier = Modifier.height(16.dp))
        PakkiePakkieGauge(percent = 75f, sizeDp = 100f)
        Spacer(modifier = Modifier.height(16.dp))
        PakkiePakkieGauge(percent = 95f, sizeDp = 100f)
        Spacer(modifier = Modifier.height(16.dp))
        PakkiePakkieGauge(percent = 72.5f, sizeDp = 140f)
    }
}

@Preview
@Composable
private fun PakkiePakkieGaugeLightPreview() {
    PreviewContainer(isDarkTheme = false) {
        PreviewContent()
    }
}

@Preview
@Composable
private fun PakkiePakkieGaugeDarkPreview() {
    PreviewContainer(isDarkTheme = true) {
        PreviewContent()
    }
}
