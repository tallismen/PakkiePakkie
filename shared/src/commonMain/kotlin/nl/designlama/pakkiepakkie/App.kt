package nl.designlama.pakkiepakkie

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nl.designlama.pakkiepakkie.app.AppNavigation
import nl.designlama.pakkiepakkie.theme.AppTheme
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer

@Composable
fun App() = AppTheme {
    AppNavigation()
}

@PreviewLightDark
@Composable
private fun AppPreview() {
    PreviewContainer {
        AppNavigation()
    }
}
