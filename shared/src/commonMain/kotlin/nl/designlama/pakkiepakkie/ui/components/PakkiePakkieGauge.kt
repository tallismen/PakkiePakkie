package nl.designlama.pakkiepakkie.ui.components

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
import kotlin.math.min
import nl.designlama.pakkiepakkie.theme.winChanceColor
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PakkiePakkieGauge(
    percent: Float?,
    modifier: Modifier = Modifier,
    sizeDp: Float = 120f,
    showLabel: Boolean = true,
) {
    val darkTheme = isSystemInDarkTheme()
    val track = MaterialTheme.colorScheme.surfaceVariant
    val brand = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val progressColor = percent?.let { winChanceColor(it, darkTheme) } ?: brand
    val sweep = ((percent ?: 0f).coerceIn(0f, 100f) / 100f) * 270f
    val dim = sizeDp.dp
    Box(modifier = modifier.size(dim), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(dim)) {
            val stroke = size.minDimension * 0.08f
            val pad = stroke / 2f + size.minDimension * 0.04f
            val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
            val topLeft = Offset(pad, pad)
            drawArc(
                color = track,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (percent != null) {
                drawArc(
                    color = progressColor,
                    startAngle = 135f,
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
                text = percent?.let { "${it.toInt()}%" } ?: "—",
                color = if (percent != null) progressColor else onSurface,
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
