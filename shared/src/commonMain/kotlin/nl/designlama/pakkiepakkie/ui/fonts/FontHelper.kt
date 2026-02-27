package nl.designlama.pakkiepakkie.ui.fonts

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Composable
expect fun getFontFamily(fontWeight: FontWeight): FontFamily

