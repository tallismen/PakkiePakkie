package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.designlama.pakkiepakkie.data.Review
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewEditorSheet(
    kenteken: String,
    rating: Int,
    text: String,
    isEditing: Boolean,
    submitting: Boolean,
    errorMessage: String?,
    onRatingChange: (Int) -> Unit,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { if (!submitting) onDismiss() },
        sheetState = sheetState,
    ) {
        ReviewEditorContent(
            kenteken = kenteken,
            rating = rating,
            text = text,
            isEditing = isEditing,
            submitting = submitting,
            errorMessage = errorMessage,
            onRatingChange = onRatingChange,
            onTextChange = onTextChange,
            onSubmit = onSubmit,
            onDismiss = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        )
    }
}

@Composable
fun ReviewEditorContent(
    kenteken: String,
    rating: Int,
    text: String,
    isEditing: Boolean,
    submitting: Boolean,
    errorMessage: String?,
    onRatingChange: (Int) -> Unit,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Beoordeling",
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatLicensePlate(kenteken),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sterren",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        StarRating(
            rating = rating,
            interactive = !submitting,
            onRatingChange = onRatingChange,
            starSizeSp = 30f,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { newValue ->
                if (newValue.length <= Review.MAX_TEXT_LENGTH) onTextChange(newValue)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !submitting,
            label = { Text("Toelichting (optioneel)") },
            supportingText = {
                Text("${text.length}/${Review.MAX_TEXT_LENGTH}")
            },
            minLines = 3,
            maxLines = 5,
        )

        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !submitting,
                modifier = Modifier.weight(1f),
            ) {
                Text("Annuleren")
            }
            Button(
                onClick = onSubmit,
                enabled = !submitting && rating in Review.MIN_RATING..Review.MAX_RATING,
                modifier = Modifier.weight(1f),
            ) {
                if (submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(if (isEditing) "Bijwerken" else "Plaatsen")
                }
            }
        }
    }
}

@Preview
@Composable
private fun ReviewEditorContentPreview() {
    PreviewContainer(isDarkTheme = false) {
        ReviewEditorContent(
            kenteken = "PL700K",
            rating = 4,
            text = "Prima auto voor dagelijks gebruik.",
            isEditing = false,
            submitting = false,
            errorMessage = null,
            onRatingChange = {},
            onTextChange = {},
            onSubmit = {},
            onDismiss = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
