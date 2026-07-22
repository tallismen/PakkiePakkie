package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import nl.designlama.pakkiepakkie.theme.AppTheme

/**
 * Preview wrapper that applies [AppTheme] from the current system / preview uiMode.
 *
 * Use with `@PreviewLightDark` so light and dark previews pick up theme automatically:
 *
 * ```
 * @PreviewLightDark
 * @Composable
 * private fun MyPreview() {
 *     PreviewContainer {
 *         MyContent()
 *     }
 * }
 * ```
 */
@Composable
fun PreviewContainer(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    AppTheme(isDarkTheme = isDarkTheme) {
        CompositionLocalProvider {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
            ) {
                content()
            }
        }
    }
}
