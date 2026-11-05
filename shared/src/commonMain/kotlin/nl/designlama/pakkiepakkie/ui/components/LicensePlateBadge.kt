package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.designlama.pakkiepakkie.theme.Black
import nl.designlama.pakkiepakkie.theme.LicensePlateBlue
import nl.designlama.pakkiepakkie.theme.LicensePlateYellow
import nl.designlama.pakkiepakkie.theme.White
import nl.designlama.pakkiepakkie.ui.extentions.nonScaledSp
import nl.designlama.pakkiepakkie.ui.fonts.getFontFamily
import org.jetbrains.compose.resources.painterResource
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.eu_flag

@Composable
fun LicensePlateBadge(
    kenteken: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val corner = if (compact) 8.dp else 10.dp
    val fontSize: TextUnit = if (compact) 14.sp.nonScaledSp else 16.sp.nonScaledSp
    val badgePadH: Dp = if (compact) 4.dp else 6.dp
    val badgePadV: Dp = if (compact) 4.dp else 6.dp
    val bodyPadH: Dp = if (compact) 10.dp else 12.dp
    val bodyPadV: Dp = if (compact) 6.dp else 8.dp
    val flagSize = if (compact) 10.dp else 12.dp
    val display = formatLicensePlate(sanitizeLicensePlate(kenteken))

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(corner)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(LicensePlateBlue)
                .padding(horizontal = badgePadH, vertical = badgePadV),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.eu_flag),
                contentDescription = null,
                modifier = Modifier.size(flagSize),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = "NL",
                color = White,
                fontSize = if (compact) 8.sp.nonScaledSp else 10.sp.nonScaledSp,
                fontWeight = FontWeight.Medium,
                fontFamily = getFontFamily(FontWeight.Medium),
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .background(LicensePlateYellow)
                .padding(horizontal = bodyPadH, vertical = bodyPadV),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = display.ifBlank { "—" },
                color = Black,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = getFontFamily(FontWeight.Black),
                letterSpacing = 1.sp,
                maxLines = 1,
            )
        }
    }
}
