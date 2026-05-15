package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.designlama.pakkiepakkie.theme.AppTheme


/**
 * A preview composable that provides a consistent background and padding for all previews.
 *
 * @param isDarkTheme If dark mode is enabled
 * @param content The content to be displayed in the preview.
 */
@Composable
fun PreviewContainer(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    AppTheme(isDarkTheme = isDarkTheme) {
        CompositionLocalProvider {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
            ) {
                content()
            }
        }
    }
}
