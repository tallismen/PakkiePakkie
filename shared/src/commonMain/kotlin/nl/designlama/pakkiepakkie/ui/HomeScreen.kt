package nl.designlama.pakkiepakkie.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import nl.designlama.pakkiepakkie.datastore.UnitPreferencesRepository
import nl.designlama.pakkiepakkie.domain.units.UnitPreferences
import nl.designlama.pakkiepakkie.domain.units.VehicleDisplayFormatter
import nl.designlama.pakkiepakkie.network.rdw.PakkiePakkieCalculator
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.DutchLicensePlateInput
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieGauge
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieText
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import nl.designlama.pakkiepakkie.ui.components.formatLicensePlate
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onOpenVehicleDetail: (String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val unitPreferences by koinInject<UnitPreferencesRepository>()
        .preferencesFlow
        .collectAsStateWithLifecycle(initialValue = UnitPreferences())
    val displayFormatter = koinInject<VehicleDisplayFormatter>()
    LaunchedEffect(viewModel) {
        viewModel.directions.collectLatest { dir ->
            when (dir) {
                is HomeDirections.OpenVehicleDetail -> onOpenVehicleDetail(dir.kenteken)
                HomeDirections.OpenSettings -> onOpenSettings()
            }
        }
    }
    HomeContent(
        state = state,
        unitPreferences = unitPreferences,
        displayFormatter = displayFormatter,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    unitPreferences: UnitPreferences,
    displayFormatter: VehicleDisplayFormatter,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            PakkiePakkieText(
                text = "PakkiePakkie",
                textColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center),
            )
            TextButton(
                onClick = { onEvent(HomeEvent.OnSettingsClick) },
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Text("⚙")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        DutchLicensePlateInput(
            value = state.licensePlateInput,
            onValueChange = { raw, _ -> onEvent(HomeEvent.OnLicensePlateChange(raw)) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(HomeEvent.OnSearchClick) },
            enabled = state.canSearch,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Zoek voertuig")
        }

        if (state.loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        state.errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            PakkiePakkieText(
                text = msg,
                textColor = MaterialTheme.colorScheme.error,
            )
        }

        state.lookupResult?.let { info ->
            Spacer(modifier = Modifier.height(16.dp))
            VehicleInfoCard(
                info = info,
                myVehicleInfo = state.myVehicleInfo,
                myVehicleKenteken = state.myVehicleKenteken,
                unitPreferences = unitPreferences,
                displayFormatter = displayFormatter,
                onSetMyVehicle = { onEvent(HomeEvent.OnSetThisAsMyVehicle) },
            )
        }

        if (state.recent.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            PakkiePakkieText(
                text = "Recent bekeken",
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                state.recent.forEach { row ->
                    ListItem(
                        headlineContent = {
                            PakkiePakkieText(text = formatLicensePlate(row.kenteken))
                        },
                        supportingContent = {
                            PakkiePakkieText(text = "${row.merk} ${row.handelsbenaming}".trim())
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PakkiePakkieGauge(
                                    percent = state.recentWinPercent[row.kenteken],
                                    sizeDp = 64f,
                                )
                                TextButton(
                                    onClick = { onEvent(HomeEvent.OnRecentSetMyVehicle(row.kenteken)) },
                                ) {
                                    Text(text = "★", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        },
                        modifier = Modifier.clickable {
                            onEvent(HomeEvent.OnRecentRowClick(row.kenteken))
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun VehicleInfoCard(
    info: VehicleLicensePlateInfo,
    myVehicleInfo: VehicleLicensePlateInfo?,
    myVehicleKenteken: String?,
    unitPreferences: UnitPreferences,
    displayFormatter: VehicleDisplayFormatter,
    onSetMyVehicle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        PakkiePakkieText(text = formatLicensePlate(info.kenteken))
        Spacer(modifier = Modifier.height(8.dp))
        PakkiePakkieText(text = "${info.merk} ${info.handelsbenaming}".trim())
        info.massaRijklaarKg?.let {
            PakkiePakkieText(
                text = "Rijklaar gewicht: ${displayFormatter.formatWeight(it, unitPreferences)}",
            )
        }
        PakkiePakkieText(
            text = "${displayFormatter.powerLabel(unitPreferences)}: ${displayFormatter.formatPower(info.vermogenKw, unitPreferences)}",
        )
        if (info.brandstofOmschrijvingen.isNotEmpty()) {
            PakkiePakkieText(text = "Brandstof: ${info.brandstofOmschrijvingen.joinToString(", ")}")
        }
        PakkiePakkieText(text = "Versnellingsbak: ${versnellingsbakDisplayNl(info.versnellingsbakCode)}")
        Spacer(modifier = Modifier.height(16.dp))
        val winPct = myVehicleInfo?.let { PakkiePakkieCalculator.winProbabilityPercent(it, info) }
        val hint = when {
            myVehicleInfo == null && !myVehicleKenteken.isNullOrBlank() ->
                "Zoek eerst je eigen auto (${formatLicensePlate(myVehicleKenteken)}) om een PakkiePakkie™ score te zien."
            myVehicleInfo == null ->
                "Kies je auto (★ bij recent of knop hieronder)."
            else ->
                "Jouw kans om te winnen vs dit voertuig"
        }
        PakkiePakkieText(
            text = hint,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        PakkiePakkieGauge(percent = winPct, sizeDp = 140f)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onSetMyVehicle, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Dit is mijn auto")
        }
    }
}

@Composable
private fun PreviewContent() {
    val formatter = VehicleDisplayFormatter()
    HomeContent(
        state = HomeState(
            licensePlateInput = "PL700K",
            lookupResult = VehicleLicensePlateInfo(
                kenteken = "PL700K",
                merk = "VOLVO",
                handelsbenaming = "V40",
                massaRijklaarKg = 1420,
                massaLedigKg = 1300,
                cilinderinhoudCc = 1596,
                vermogenMassaRijklaar = 0.08,
                vermogenKw = 110.0,
                brandstofOmschrijvingen = listOf("Benzine"),
                hybridKlasse = null,
                versnellingsbakCode = "M",
                aantalVersnellingen = 6,
            ),
            recent = emptyList(),
            errorMessage = "Fout",
        ),
        unitPreferences = UnitPreferences(),
        displayFormatter = formatter,
        onEvent = {},
    )
}

@Preview
@Composable
private fun HomeScreenLightPreview() {
    PreviewContainer(isDarkTheme = false) {
        PreviewContent()
    }
}

@Preview
@Composable
private fun HomeScreenDarkPreview() {
    PreviewContainer(isDarkTheme = true) {
        PreviewContent()
    }
}
