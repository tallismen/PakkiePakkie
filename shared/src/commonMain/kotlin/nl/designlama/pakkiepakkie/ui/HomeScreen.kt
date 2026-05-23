package nl.designlama.pakkiepakkie.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import nl.designlama.pakkiepakkie.data.local.VehicleLookupEntity
import nl.designlama.pakkiepakkie.ui.components.DutchLicensePlateInput
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieGauge
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieText
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieTopBar
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import nl.designlama.pakkiepakkie.ui.components.formatLicensePlate
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onOpenVehicleDetail: (String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PakkiePakkieTopBar(
                title = "PakkiePakkie",
                actions = {
                    IconButton(onClick = { onEvent(HomeEvent.OnSettingsClick) }) {
                        Text(
                            text = "⚙",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                DutchLicensePlateInput(
                    value = state.licensePlateInput,
                    onValueChange = { raw, _ -> onEvent(HomeEvent.OnLicensePlateChange(raw)) },
                    showClearButton = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                    Spacer(modifier = Modifier.height(12.dp))
                    PakkiePakkieText(
                        text = msg,
                        textColor = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (state.recent.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Text(
                    text = "Recent bekeken",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                ) {
                    state.recent.forEach { row ->
                        RecentVehicleRow(
                            row = row,
                            winPercent = state.recentWinPercent[row.kenteken],
                            isMyVehicle = isSameKenteken(row.kenteken, state.myVehicleKenteken),
                            onClick = { onEvent(HomeEvent.OnRecentRowClick(row.kenteken)) },
                            onSetMyVehicle = { onEvent(HomeEvent.OnRecentSetMyVehicle(row.kenteken)) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentVehicleRow(
    row: VehicleLookupEntity,
    winPercent: Float?,
    isMyVehicle: Boolean,
    onClick: () -> Unit,
    onSetMyVehicle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick)
                .padding(start = 16.dp),
        ) {
            Text(
                text = formatLicensePlate(row.kenteken),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${row.merk} ${row.handelsbenaming}".trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        PakkiePakkieGauge(
            percent = winPercent,
            sizeDp = 52f,
            showLabel = false,
            animationEnabled = false,
        )
        IconButton(onClick = onSetMyVehicle, modifier = Modifier.padding(end = 4.dp)) {
            Text(
                text = if (isMyVehicle) "★" else "☆",
                style = MaterialTheme.typography.titleMedium,
                color = if (isMyVehicle) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

private fun isSameKenteken(a: String, b: String?): Boolean {
    if (b == null) return false
    return sanitizeLicensePlate(a) == sanitizeLicensePlate(b)
}

@Composable
private fun PreviewContent() {
    HomeContent(
        state = HomeState(
            licensePlateInput = "PL700K",
            recent = listOf(
                VehicleLookupEntity(
                    kenteken = "PL700K",
                    merk = "VOLVO",
                    handelsbenaming = "V40",
                    massaRijklaarKg = 1420,
                    vermogenKw = 110.0,
                    vermogenPk = 149.6,
                    massaLedigKg = 1300,
                    cilinderinhoudCc = 1596,
                    vermogenMassaRijklaar = 0.08,
                    primaryBrandstof = "Benzine",
                    hybridKlasse = null,
                    brandstofJson = "[\"Benzine\"]",
                    versnellingsbakCode = "M",
                    aantalVersnellingen = 6,
                    dataVersion = 2,
                    lastViewedAt = 0L,
                    lastFetchedAt = 0L,
                    maximaleConstructiesnelheidKmh = 210,
                    isChipped = false,
                ),
            ),
            myVehicleKenteken = "PL700K",
            recentWinPercent = mapOf("PL700K" to 72f),
            errorMessage = null,
        ),
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
