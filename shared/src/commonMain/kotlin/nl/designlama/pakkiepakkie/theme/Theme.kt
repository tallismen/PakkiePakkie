package nl.designlama.pakkiepakkie.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    background = White,
    secondary = Secondary,
    onPrimary = White,
    onSecondary = White,
    error = Red,
    onError = White,
)

@Composable
internal fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = { Surface(content = content) }
    )
}
