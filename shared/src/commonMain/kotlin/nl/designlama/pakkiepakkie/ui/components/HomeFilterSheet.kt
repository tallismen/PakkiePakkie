package nl.designlama.pakkiepakkie.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import nl.designlama.pakkiepakkie.theme.LicensePlateBlue
import nl.designlama.pakkiepakkie.theme.White
import nl.designlama.pakkiepakkie.ui.HomeFilter
import nl.designlama.pakkiepakkie.ui.HomeSortBy
import org.jetbrains.compose.resources.stringResource
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.filter_max
import pakkiepakkie.shared.generated.resources.filter_min
import pakkiepakkie.shared.generated.resources.filter_power_label
import pakkiepakkie.shared.generated.resources.filter_reset
import pakkiepakkie.shared.generated.resources.filter_score_label
import pakkiepakkie.shared.generated.resources.filter_show_results
import pakkiepakkie.shared.generated.resources.filter_sort_label
import pakkiepakkie.shared.generated.resources.filter_sort_power
import pakkiepakkie.shared.generated.resources.filter_sort_score
import pakkiepakkie.shared.generated.resources.filter_sort_year
import pakkiepakkie.shared.generated.resources.filter_title
import pakkiepakkie.shared.generated.resources.filter_year_from
import pakkiepakkie.shared.generated.resources.filter_year_label
import pakkiepakkie.shared.generated.resources.filter_year_to

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFilterSheet(
    initial: HomeFilter,
    resultCountFor: (HomeFilter) -> Int,
    onApply: (HomeFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember(initial) { mutableStateOf(initial) }
    val resultCount = resultCountFor(draft)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.filter_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { draft = HomeFilter() },
                ) {
                    Text(
                        text = stringResource(Res.string.filter_reset),
                        fontWeight = FontWeight.SemiBold,
                        color = LicensePlateBlue,
                    )
                }
            }

            Text(
                text = stringResource(Res.string.filter_sort_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortChip(
                    label = stringResource(Res.string.filter_sort_score),
                    selected = draft.sortBy == HomeSortBy.Score,
                    onClick = { draft = draft.copy(sortBy = HomeSortBy.Score) },
                )
                SortChip(
                    label = stringResource(Res.string.filter_sort_year),
                    selected = draft.sortBy == HomeSortBy.Bouwjaar,
                    onClick = { draft = draft.copy(sortBy = HomeSortBy.Bouwjaar) },
                )
                SortChip(
                    label = stringResource(Res.string.filter_sort_power),
                    selected = draft.sortBy == HomeSortBy.Vermogen,
                    onClick = { draft = draft.copy(sortBy = HomeSortBy.Vermogen) },
                )
            }

            FilterRangeRow(
                title = stringResource(Res.string.filter_score_label),
                leftLabel = stringResource(Res.string.filter_min),
                rightLabel = stringResource(Res.string.filter_max),
                leftValue = draft.scoreMin,
                rightValue = draft.scoreMax,
                onLeftChange = { draft = draft.copy(scoreMin = it) },
                onRightChange = { draft = draft.copy(scoreMax = it) },
            )
            FilterRangeRow(
                title = stringResource(Res.string.filter_year_label),
                leftLabel = stringResource(Res.string.filter_year_from),
                rightLabel = stringResource(Res.string.filter_year_to),
                leftValue = draft.yearFrom,
                rightValue = draft.yearTo,
                onLeftChange = { draft = draft.copy(yearFrom = it) },
                onRightChange = { draft = draft.copy(yearTo = it) },
            )
            FilterRangeRow(
                title = stringResource(Res.string.filter_power_label),
                leftLabel = stringResource(Res.string.filter_min),
                rightLabel = stringResource(Res.string.filter_max),
                leftValue = draft.pkMin,
                rightValue = draft.pkMax,
                onLeftChange = { draft = draft.copy(pkMin = it) },
                onRightChange = { draft = draft.copy(pkMax = it) },
            )

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = { onApply(draft) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LicensePlateBlue,
                    contentColor = White,
                ),
            ) {
                Text(
                    text = stringResource(Res.string.filter_show_results, resultCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = LicensePlateBlue.copy(alpha = 0.12f),
            selectedLabelColor = LicensePlateBlue,
        ),
    )
}

@Composable
private fun FilterRangeRow(
    title: String,
    leftLabel: String,
    rightLabel: String,
    leftValue: Int?,
    rightValue: Int?,
    onLeftChange: (Int?) -> Unit,
    onRightChange: (Int?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberFilterField(
                label = leftLabel,
                value = leftValue,
                onValueChange = onLeftChange,
                modifier = Modifier.weight(1f),
            )
            NumberFilterField(
                label = rightLabel,
                value = rightValue,
                onValueChange = onRightChange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NumberFilterField(
    label: String,
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value?.toString().orEmpty(),
        onValueChange = { raw ->
            val digits = raw.filter { it.isDigit() }.take(4)
            onValueChange(digits.toIntOrNull())
        },
        modifier = modifier,
        label = { Text(label) },
        placeholder = { Text("-") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
    )
}
