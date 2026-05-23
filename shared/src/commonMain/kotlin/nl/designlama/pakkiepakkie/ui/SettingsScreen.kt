package nl.designlama.pakkiepakkie.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.designlama.pakkiepakkie.domain.units.PowerUnit
import nl.designlama.pakkiepakkie.domain.units.UnitPreferences
import nl.designlama.pakkiepakkie.domain.units.UnitPreset
import nl.designlama.pakkiepakkie.domain.units.WeightUnit
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieTopBar
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsContent(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PakkiePakkieTopBar(
                title = "Instellingen",
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Text(
                    text = "Voorbeeld: ${state.previewPower} · ${state.previewWeight}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }

            SettingsSection(title = "Preset") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    UnitPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = state.preferences.preset == preset,
                            onClick = { onEvent(SettingsEvent.OnPresetSelected(preset)) },
                            label = { Text(presetLabel(preset)) },
                        )
                    }
                }
            }

            SettingsSection(title = "Vermogen") {
                PowerUnit.entries.forEach { unit ->
                    UnitRadioRow(
                        label = powerUnitLabel(unit),
                        selected = state.preferences.powerUnit == unit,
                        onClick = { onEvent(SettingsEvent.OnPowerUnitSelected(unit)) },
                    )
                }
            }

            SettingsSection(title = "Gewicht") {
                WeightUnit.entries.forEach { unit ->
                    UnitRadioRow(
                        label = weightUnitLabel(unit),
                        selected = state.preferences.weightUnit == unit,
                        onClick = { onEvent(SettingsEvent.OnWeightUnitSelected(unit)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun UnitRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

private fun presetLabel(preset: UnitPreset): String = when (preset) {
    UnitPreset.Standaard -> "Standaard"
    UnitPreset.Metric -> "Metric"
    UnitPreset.Imperial -> "Imperial"
    UnitPreset.PakkiePakkie -> "PakkiePakkie"
}

private fun powerUnitLabel(unit: PowerUnit): String = when (unit) {
    PowerUnit.Kw -> "kW"
    PowerUnit.Hp -> "HP"
    PowerUnit.Pakkies -> "Pakkies"
}

private fun weightUnitLabel(unit: WeightUnit): String = when (unit) {
    WeightUnit.Kg -> "kg"
    WeightUnit.Lbs -> "lbs"
    WeightUnit.Slippers -> "slippers"
}

@Composable
private fun SettingsPreviewContent() {
    SettingsContent(
        state = SettingsState(
            preferences = UnitPreferences(),
            previewPower = "110 kW (150 pk)",
            previewWeight = "1420 kg",
        ),
        onEvent = {},
        onBack = {},
    )
}

@Preview
@Composable
private fun SettingsScreenLightPreview() {
    PreviewContainer(isDarkTheme = false) {
        SettingsPreviewContent()
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    PreviewContainer(isDarkTheme = true) {
        SettingsPreviewContent()
    }
}
