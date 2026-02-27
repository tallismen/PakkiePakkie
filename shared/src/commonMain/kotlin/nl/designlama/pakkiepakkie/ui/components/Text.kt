package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.designlama.pakkiepakkie.theme.Black
import nl.designlama.pakkiepakkie.ui.extentions.nonScaledSp
import nl.designlama.pakkiepakkie.ui.fonts.getFontFamily
import org.jetbrains.compose.ui.tooling.preview.Preview


@Preview
@Composable
private fun TextPreview() {
    Preview {
        PakkiePakkieText("Preview", fontWeight = FontWeight.Normal)
        PakkiePakkieText("Preview Bold", fontWeight = FontWeight.Bold)
        PakkiePakkieText(
            "With Leading Icon", fontWeight = FontWeight.Bold,
//            leadingIcon = TextIcon(
//                icon = painterResource(Res.drawable.ic_cross)
//            )
        )
        PakkiePakkieText(
            "With Trailing Icon", fontWeight = FontWeight.Bold,
//            trailingIcon = TextIcon(
//                icon = painterResource(Res.drawable.ic_check)
//            )
        )
    }
}

/**
 * Describes an icon that can be placed alongside text in [PakkiePakkieText] or [PakkiePakkieButton].
 *
 * @property icon The painter resource for the icon.
 * @property tint Optional tint color. When null, uses [LocalContentColor].
 * @property padding Spacing between the icon and adjacent text. Default is 12.dp.
 * @property size Display size of the icon. Default is 18.dp.
 * @property modifier Modifier to be applied to the icon.
 */
data class TextIcon(
    val icon: Painter,
    val tint: Color? = null,
    val padding: Dp = 12.dp,
    val size: Dp = 18.dp,
    val modifier: Modifier = Modifier,
) {
    @Composable
    fun Component() {
        Icon(
            icon,
            contentDescription = null,
            modifier = modifier.size(size),
            tint = tint ?: LocalContentColor.current
        )
    }
}

/**
 * Composable function to display styled text with optional leading/trailing icons.
 *
 * @param text The text to display.
 * @param textColor The color of the text. Default is [Black]. Ignored when [textBrush] is set.
 * @param textBrush Optional gradient brush for the text. When set, takes precedence over [textColor].
 * @param fontSize The size of the text. Default is 16.sp.nonScaledSp.
 * @param fontWeight The weight of the font. Default is [FontWeight.SemiBold].
 * @param textAlign Horizontal alignment of the text. Default is null (unspecified).
 * @param maxLines Maximum number of lines. Default is [Int.MAX_VALUE] (unlimited).
 * @param singleLine When true, restricts text to one line with ellipsis overflow.
 * @param leadingIcon Optional [TextIcon] displayed before the text.
 * @param trailingIcon Optional [TextIcon] displayed after the text.
 * @param textDecoration Optional decoration (e.g. underline) applied to the text.
 * @param modifier Modifier to be applied to the text.
 */
@Composable
fun PakkiePakkieText(
    text: String,
    textColor: Color = Black,
    textBrush: Brush? = null,
    fontSize: TextUnit = 16.sp.nonScaledSp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    singleLine: Boolean = false,
    leadingIcon: TextIcon? = null,
    trailingIcon: TextIcon? = null,
    textDecoration: TextDecoration? = null,
    modifier: Modifier = Modifier
) {
    val resolvedMaxLines = if (singleLine) 1 else maxLines
    val resolvedOverflow = if (singleLine || maxLines != Int.MAX_VALUE) TextOverflow.Ellipsis else TextOverflow.Clip

    val brushModifier = if (textBrush != null) {
        Modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(brush = textBrush, blendMode = BlendMode.SrcIn)
            }
    } else {
        Modifier
    }

    Row(
        modifier = brushModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.let {
            it.Component()
            Spacer(modifier = Modifier.width(it.padding))
        }
        Text(
            text = text,
            style = TextStyle(
                fontFamily = getFontFamily(fontWeight),
                color = textColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = textAlign ?: TextAlign.Unspecified,
                textDecoration = textDecoration,
                lineHeight = TextUnit(fontSize.value * 1.5f, fontSize.type),
                letterSpacing = 0.sp,
            ),
            maxLines = resolvedMaxLines,
            overflow = resolvedOverflow,
            modifier = if (leadingIcon != null || trailingIcon != null) {
                Modifier.weight(1f, fill = true).then(modifier)
            } else {
                modifier
            },
        )

        trailingIcon?.let {
            Spacer(modifier = Modifier.width(it.padding))
            it.Component()
        }
    }
}
