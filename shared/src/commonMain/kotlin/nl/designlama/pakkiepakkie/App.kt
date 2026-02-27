package nl.designlama.pakkiepakkie

import androidx.compose.runtime.Composable
import nl.designlama.pakkiepakkie.app.AppNavigation
import nl.designlama.pakkiepakkie.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun App() = AppTheme {
    AppNavigation()
}
