package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.designlama.pakkiepakkie.theme.Black
import nl.designlama.pakkiepakkie.theme.LicensePlateBlue
import nl.designlama.pakkiepakkie.theme.LicensePlateYellow
import nl.designlama.pakkiepakkie.theme.White
import nl.designlama.pakkiepakkie.ui.extentions.nonScaledSp
import nl.designlama.pakkiepakkie.ui.fonts.getFontFamily
import org.jetbrains.compose.ui.tooling.preview.Preview

private object LicensePlateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        val formatted = formatLicensePlate(rawText)
        return TransformedText(
            AnnotatedString(formatted),
            LicensePlateOffsetMapping(rawText),
        )
    }
}

private class LicensePlateOffsetMapping(private val rawText: String) : OffsetMapping {

    private val formatted: String
        get() = formatLicensePlate(rawText)

    override fun originalToTransformed(offset: Int): Int {
        if (offset <= 0) return 0
        if (offset >= rawText.length) return formatted.length
        return formatLicensePlate(rawText.take(offset)).length
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (offset <= 0) return 0
        val display = formatted
        if (offset >= display.length) return rawText.length
        for (o in 0..rawText.length) {
            if (formatLicensePlate(rawText.take(o)).length >= offset) return o
        }
        return rawText.length
    }
}

/**
 * Single-line text field styled as a Dutch license plate (blue NL strip, yellow body, bold black text).
 *
 * @param value Raw input: uppercase letters and digits only, no dashes (max 6 characters).
 *   Input is limited to prefixes allowed by RDW sidecodes 1–14 (valid letter/digit block lengths).
 * @param onValueChange Called with sanitized raw text and [formatLicensePlate] display text for convenience.
 */
@Composable
fun DutchLicensePlateInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (raw: String, formatted: String) -> Unit,
    enabled: Boolean = true,
) {
    val textStyle = TextStyle(
        color = Black,
        fontSize = 28.sp.nonScaledSp,
        fontWeight = FontWeight.Bold,
        fontFamily = getFontFamily(FontWeight.Black),
        textAlign = TextAlign.Center,
        letterSpacing = 4.sp,
    )
    val visualTransformation = remember { LicensePlateVisualTransformation }

    Row(
        modifier = modifier.height(IntrinsicSize.Min).clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(LicensePlateBlue)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            PakkiePakkieText(
                text = "NL",
                textColor = White,
                fontSize = 16.sp.nonScaledSp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        BasicTextField(
            modifier = Modifier
                .background(LicensePlateYellow)
                .padding(vertical = 8.dp),
            value = value,
            onValueChange = { new ->
                val sanitized = sanitizeDutchLicensePlateInput(new)
                onValueChange(sanitized, formatLicensePlate(sanitized))
            },
            enabled = enabled,
            textStyle = textStyle,
            singleLine = true,
            cursorBrush = SolidColor(Black),
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions.Default,
        )
    }
}

@Preview
@Composable
private fun DutchLicensePlateInputPreview() {
    Preview {
        Column {
            DutchLicensePlateInput(
                value = "",
                onValueChange = { _, _ -> },
            )
            Spacer(modifier = Modifier.height(12.dp))
            DutchLicensePlateInput(
                value = "PL7",
                onValueChange = { _, _ -> },
            )
            Spacer(modifier = Modifier.height(12.dp))
            DutchLicensePlateInput(
                value = "PL700K",
                onValueChange = { _, _ -> },
            )
            Spacer(modifier = Modifier.height(12.dp))
            DutchLicensePlateInput(
                value = "GBB01B",
                onValueChange = { _, _ -> },
            )
            Spacer(modifier = Modifier.height(12.dp))
            var interactive by remember { mutableStateOf("") }
            DutchLicensePlateInput(
                value = interactive,
                onValueChange = { raw, _ -> interactive = raw },
            )
        }
    }
}
