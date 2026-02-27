package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.designlama.pakkiepakkie.theme.White


/**
 * A preview composable that provides a consistent background and padding for all previews.
 *
 * @param content The content to be displayed in the preview.
 */
@Composable
fun Preview(content: @Composable () -> Unit) {
    CompositionLocalProvider() {
        Column(modifier = Modifier.background(White).padding(16.dp)) {
            content()
        }
    }
}
