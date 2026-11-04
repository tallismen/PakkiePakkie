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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
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
import org.jetbrains.compose.resources.stringResource
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.home_search_placeholder

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

enum class LicensePlateInputStyle {
    /** Classic tall Dutch plate (centered text). */
    Plate,

    /** Yellow search field with NL badge, placeholder, and clear inside the bar. */
    Search,
}

/**
 * Single-line text field styled as a Dutch license plate (blue NL strip, yellow body).
 *
 * @param value Raw input: uppercase letters and digits only, no dashes (max 6 characters).
 * @param onValueChange Called with sanitized raw text and [formatLicensePlate] display text.
 * @param showClearButton When true, shows an × control (inside the yellow bar for [LicensePlateInputStyle.Search]).
 * @param onDone Optional IME Done action (e.g. trigger search).
 */
@Composable
fun DutchLicensePlateInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (raw: String, formatted: String) -> Unit,
    enabled: Boolean = true,
    showClearButton: Boolean = false,
    style: LicensePlateInputStyle = LicensePlateInputStyle.Plate,
    placeholder: String? = null,
    onDone: (() -> Unit)? = null,
    compact: Boolean = false,
) {
    when (style) {
        LicensePlateInputStyle.Plate -> PlateStyleInput(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            showClearButton = showClearButton,
            onDone = onDone,
        )
        LicensePlateInputStyle.Search -> SearchStyleInput(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            showClearButton = showClearButton,
            placeholder = placeholder ?: stringResource(Res.string.home_search_placeholder),
            onDone = onDone,
            compact = compact,
        )
    }
}

@Composable
private fun PlateStyleInput(
    modifier: Modifier,
    value: String,
    onValueChange: (raw: String, formatted: String) -> Unit,
    enabled: Boolean,
    showClearButton: Boolean,
    onDone: (() -> Unit)?,
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
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NlBadge(horizontalPadding = 10.dp, verticalPadding = 5.dp, fontSize = 16.sp.nonScaledSp)
            BasicTextField(
                modifier = Modifier
                    .weight(1f)
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
                    imeAction = if (onDone != null) ImeAction.Search else ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDone?.invoke() },
                    onSearch = { onDone?.invoke() },
                ),
            )
        }

        if (showClearButton && value.isNotEmpty()) {
            IconButton(
                onClick = { onValueChange("", formatLicensePlate("")) },
                enabled = enabled,
            ) {
                Text(
                    text = "×",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SearchStyleInput(
    modifier: Modifier,
    value: String,
    onValueChange: (raw: String, formatted: String) -> Unit,
    enabled: Boolean,
    showClearButton: Boolean,
    placeholder: String,
    onDone: (() -> Unit)?,
    compact: Boolean,
) {
    val fontSize: TextUnit = if (compact) 16.sp.nonScaledSp else 18.sp.nonScaledSp
    val verticalPadding = if (compact) 8.dp else 12.dp
    val corner = if (compact) 12.dp else 14.dp
    val textStyle = TextStyle(
        color = Black,
        fontSize = fontSize,
        fontWeight = FontWeight.SemiBold,
        fontFamily = getFontFamily(FontWeight.Bold),
        textAlign = TextAlign.Start,
        letterSpacing = 1.sp,
    )
    val visualTransformation = remember { LicensePlateVisualTransformation }

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(corner))
            .background(LicensePlateYellow),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NlBadge(
            horizontalPadding = if (compact) 8.dp else 10.dp,
            verticalPadding = if (compact) 6.dp else 8.dp,
            fontSize = if (compact) 12.sp.nonScaledSp else 14.sp.nonScaledSp,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = verticalPadding),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = textStyle.copy(
                        color = Black.copy(alpha = 0.45f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp,
                    ),
                    maxLines = 1,
                )
            }
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
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
                    imeAction = if (onDone != null) ImeAction.Search else ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDone?.invoke() },
                    onSearch = { onDone?.invoke() },
                ),
            )
        }
        if (showClearButton && value.isNotEmpty()) {
            IconButton(
                onClick = { onValueChange("", formatLicensePlate("")) },
                enabled = enabled,
                modifier = Modifier.size(if (compact) 36.dp else 40.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Black.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "×",
                        color = Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun NlBadge(
    horizontalPadding: Dp,
    verticalPadding: Dp,
    fontSize: TextUnit,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .background(LicensePlateBlue)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        PakkiePakkieText(
            text = "NL",
            textColor = White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PreviewContent() {
    Column {
        DutchLicensePlateInput(
            value = "",
            onValueChange = { _, _ -> },
            style = LicensePlateInputStyle.Search,
            showClearButton = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        DutchLicensePlateInput(
            value = "56ZKD3",
            onValueChange = { _, _ -> },
            style = LicensePlateInputStyle.Search,
            showClearButton = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        DutchLicensePlateInput(
            value = "56ZKD3",
            onValueChange = { _, _ -> },
            style = LicensePlateInputStyle.Search,
            showClearButton = true,
            compact = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        DutchLicensePlateInput(
            value = "PL700K",
            onValueChange = { _, _ -> },
        )
        Spacer(modifier = Modifier.height(12.dp))
        var interactive by remember { mutableStateOf("") }
        DutchLicensePlateInput(
            value = interactive,
            onValueChange = { raw, _ -> interactive = raw },
            style = LicensePlateInputStyle.Search,
            showClearButton = true,
        )
    }
}

@PreviewLightDark
@Composable
private fun DutchLicensePlateInputPreview() {
    PreviewContainer {
        PreviewContent()
    }
}
