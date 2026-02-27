package nl.designlama.pakkiepakkie.ui.fonts

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.Roboto


@Composable
actual fun getFontFamily(fontWeight: FontWeight): FontFamily {
    return FontFamily(Font(Res.font.Roboto, fontWeight))
}