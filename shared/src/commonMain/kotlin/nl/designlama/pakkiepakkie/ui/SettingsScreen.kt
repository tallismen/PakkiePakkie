package nl.designlama.pakkiepakkie.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import nl.designlama.pakkiepakkie.datastore.TireType
import nl.designlama.pakkiepakkie.domain.units.PowerUnit
import nl.designlama.pakkiepakkie.domain.units.UnitPreferences
import nl.designlama.pakkiepakkie.domain.units.UnitPreset
import nl.designlama.pakkiepakkie.ui.components.FormRow
import nl.designlama.pakkiepakkie.ui.components.PakkieCard
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import nl.designlama.pakkiepakkie.ui.components.StickyGlassHeaderBar
import nl.designlama.pakkiepakkie.ui.components.UiSymbols
import nl.designlama.pakkiepakkie.ui.components.formatLicensePlate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.app_logo
import pakkiepakkie.shared.generated.resources.format_power_pk
import pakkiepakkie.shared.generated.resources.home_list_item_subtitle
import pakkiepakkie.shared.generated.resources.profile_delete_vehicle
import pakkiepakkie.shared.generated.resources.profile_label_chipped
import pakkiepakkie.shared.generated.resources.profile_label_mileage
import pakkiepakkie.shared.generated.resources.profile_label_modded_exhaust
import pakkiepakkie.shared.generated.resources.profile_label_nitrous
import pakkiepakkie.shared.generated.resources.profile_label_tires
import pakkiepakkie.shared.generated.resources.profile_no_vehicle
import pakkiepakkie.shared.generated.resources.profile_section_units
import pakkiepakkie.shared.generated.resources.profile_tires_allseason
import pakkiepakkie.shared.generated.resources.profile_tires_summer
import pakkiepakkie.shared.generated.resources.profile_tires_winter
import pakkiepakkie.shared.generated.resources.profile_title
import pakkiepakkie.shared.generated.resources.settings_section_power
import pakkiepakkie.shared.generated.resources.settings_version_format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenTestMenu: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.directions.collectLatest { dir ->
            when (dir) {
                SettingsDirections.OpenTestMenu -> onOpenTestMenu()
            }
        }
    }
    SettingsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        // Top/horizontal insets are handled by StickyGlassHeader; keep bottom for home indicator.
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal,
        ),
        topBar = {
            StickyGlassHeaderBar(
                title = stringResource(Res.string.profile_title),
                onBack = onBack,
                elevated = scroll.value > 8,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            VehicleProfileCard(state = state, onEvent = onEvent)

            SettingsSection(title = stringResource(Res.string.profile_section_units)) {
                UnitPreset.entries.filter {
                    it == UnitPreset.Metric || it == UnitPreset.Imperial || it == UnitPreset.PakkiePakkie
                }.forEach { preset ->
                    UnitRadioRow(
                        label = unitPresetLabel(preset),
                        selected = state.preferences.preset == preset,
                        onClick = { onEvent(SettingsEvent.OnPresetSelected(preset)) },
                    )
                }
            }

            SettingsSection(title = stringResource(Res.string.settings_section_power)) {
                PowerUnit.entries.forEach { unit ->
                    UnitRadioRow(
                        label = powerUnitLabel(unit),
                        selected = state.preferences.powerUnit == unit,
                        onClick = { onEvent(SettingsEvent.OnPowerUnitSelected(unit)) },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            SettingsVersionFooter(
                versionName = state.versionName,
                versionCode = state.versionCode,
                onVersionClick = { onEvent(SettingsEvent.OnVersionTapped) },
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VehicleProfileCard(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
) {
    val info = state.myVehicleInfo
    val focusManager = LocalFocusManager.current
    PakkieCard {
        if (info == null) {
            Text(
                text = stringResource(Res.string.profile_no_vehicle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            val pk = info.vermogenPk?.toInt()
            Text(
                text = stringResource(
                    Res.string.home_list_item_subtitle,
                    formatLicensePlate(info.kenteken),
                    pk?.let { stringResource(Res.string.format_power_pk, it) } ?: "—",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            )

            FormRow(icon = UiSymbols.MILEAGE, label = stringResource(Res.string.profile_label_mileage)) {
                BasicTextField(
                    value = state.mileageInput,
                    onValueChange = { onEvent(SettingsEvent.OnMileageChange(it)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onEvent(SettingsEvent.OnMileageCommit)
                            focusManager.clearFocus()
                        },
                    ),
                    modifier = Modifier
                        .width(110.dp)
                        .padding(4.dp),
                    decorationBox = { inner ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                            ),
                        ) {
                            BoxPad { inner() }
                        }
                    },
                )
            }

            FormRow(icon = UiSymbols.TIRES, label = stringResource(Res.string.profile_label_tires)) {
                TireDropdown(
                    selected = state.profile.tires,
                    onSelected = { onEvent(SettingsEvent.OnTiresSelected(it)) },
                )
            }

            FormRow(icon = UiSymbols.CHIP, label = stringResource(Res.string.profile_label_chipped)) {
                Switch(
                    checked = state.myIsChipped,
                    onCheckedChange = { onEvent(SettingsEvent.OnToggleChipped) },
                )
            }
            FormRow(icon = UiSymbols.NITROUS, label = stringResource(Res.string.profile_label_nitrous)) {
                Switch(
                    checked = state.profile.hasNitrous,
                    onCheckedChange = { onEvent(SettingsEvent.OnToggleNitrous) },
                )
            }
            FormRow(
                icon = UiSymbols.EXHAUST,
                label = stringResource(Res.string.profile_label_modded_exhaust),
            ) {
                Switch(
                    checked = state.profile.hasModdedExhaust,
                    onCheckedChange = { onEvent(SettingsEvent.OnToggleModdedExhaust) },
                )
            }

            Text(
                text = stringResource(Res.string.profile_delete_vehicle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onEvent(SettingsEvent.OnDeleteVehicle) }
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun BoxPad(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
        content()
    }
}

@Composable
private fun TireDropdown(
    selected: TireType,
    onSelected: (TireType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(
            text = tireLabel(selected),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TireType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(tireLabel(type)) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun tireLabel(tires: TireType): String = when (tires) {
    TireType.Summer -> stringResource(Res.string.profile_tires_summer)
    TireType.Winter -> stringResource(Res.string.profile_tires_winter)
    TireType.AllSeason -> stringResource(Res.string.profile_tires_allseason)
}

@Composable
private fun SettingsVersionFooter(
    versionName: String,
    versionCode: String,
    onVersionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(
                Res.string.settings_version_format,
                versionName,
                versionCode,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable(onClick = onVersionClick),
        )
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

@Composable
private fun SettingsPreviewContent() {
    SettingsContent(
        state = SettingsState(
            preferences = UnitPreferences(),
            previewPower = "110 kW (150 pk)",
            previewWeight = "1420 kg",
            versionName = "1.0.0",
            versionCode = "1",
        ),
        onEvent = {},
        onBack = {},
    )
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    PreviewContainer {
        SettingsPreviewContent()
    }
}
