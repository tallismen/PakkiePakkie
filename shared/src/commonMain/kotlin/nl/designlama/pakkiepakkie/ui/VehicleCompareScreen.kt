package nl.designlama.pakkiepakkie.ui

import androidx.compose.animation.animateContentSize
import nl.designlama.pakkiepakkie.ui.components.PakkieAnimations
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.designlama.pakkiepakkie.datastore.UnitPreferencesRepository
import nl.designlama.pakkiepakkie.domain.units.UnitPreferences
import nl.designlama.pakkiepakkie.domain.units.VehicleDisplayFormatter
import nl.designlama.pakkiepakkie.network.chipped.ChippedTuneCalculator
import nl.designlama.pakkiepakkie.network.chipped.ChippedTuneEstimate
import nl.designlama.pakkiepakkie.network.rdw.PakkiePakkieCalculator
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieGauge
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieText
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieTopBar
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import nl.designlama.pakkiepakkie.ui.components.ChippedKentekenTitle
import nl.designlama.pakkiepakkie.ui.components.ChippedVehicleCard
import nl.designlama.pakkiepakkie.ui.components.formatLicensePlate
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun versnellingsbakDisplayNl(code: String?): String = when (code?.trim()?.uppercase()) {
    "A" -> "Automaat"
    "M" -> "Handgeschakeld"
    "C" -> "CVT"
    "G", "D" -> "Dubbele koppeling / robot"
    null, "" -> "—"
    else -> code.trim()
}

private enum class CompareTrend { DetailBetter, DetailWorse, Neutral }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCompareScreen(
    kenteken: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VehicleDetailViewModel = koinViewModel(parameters = { parametersOf(kenteken) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val unitPreferences by koinInject<UnitPreferencesRepository>()
        .preferencesFlow
        .collectAsStateWithLifecycle(initialValue = UnitPreferences())
    val displayFormatter = koinInject<VehicleDisplayFormatter>()
    VehicleCompareScaffold(
        kenteken = kenteken,
        state = state,
        unitPreferences = unitPreferences,
        displayFormatter = displayFormatter,
        onBack = onBack,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleCompareScaffold(
    kenteken: String,
    state: VehicleDetailState,
    unitPreferences: UnitPreferences,
    displayFormatter: VehicleDisplayFormatter,
    onBack: () -> Unit,
    onEvent: (VehicleDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            PakkiePakkieTopBar(
                titleContent = {
                    ChippedKentekenTitle(
                        kenteken = kenteken,
                        isChipped = state.detailIsChipped,
                    )
                },
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        when {
            state.loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PakkiePakkieText(text = state.errorMessage.orEmpty(), textColor = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Sluiten") }
                }
            }

            state.detail != null -> {
                VehicleCompareContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    detail = state.detail!!,
                    state = state,
                    unitPreferences = unitPreferences,
                    displayFormatter = displayFormatter,
                    onEvent = onEvent,
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun VehicleCompareContent(
    detail: VehicleLicensePlateInfo,
    state: VehicleDetailState,
    unitPreferences: UnitPreferences,
    displayFormatter: VehicleDisplayFormatter,
    onEvent: (VehicleDetailEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val my = state.my
    val myKw = my?.let {
        PakkiePakkieCalculator.effectiveVermogenKw(it, state.myTune, state.myIsChipped)
    }
    val detailKw = PakkiePakkieCalculator.effectiveVermogenKw(detail, state.detailTune, state.detailIsChipped)
    val winPct = my?.let {
        PakkiePakkieCalculator.winProbabilityPercent(
            my = it,
            other = detail,
            myVermogenKwOverride = myKw,
            otherVermogenKwOverride = detailKw,
        )
    }
    val isMyVehicle = state.isMyVehicle(detail.kenteken)

    Column(
        modifier = modifier.animateContentSize(
            animationSpec = PakkieAnimations.contentSizeSpec(),
        ),
    ) {
        Text(
            text = "${detail.merk} ${detail.handelsbenaming}".trim(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Jouw kans om te winnen vs dit voertuig",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                PakkiePakkieGauge(percent = winPct, sizeDp = 140f)
                if (my != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Schatting — nooit 0% of 100% omdat echte races variabel zijn.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                if (my == null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Stel je auto in via ☆ op de startpagina om een percentage te zien.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        ChippedVehicleCard(
            caption = "Dit voertuig",
            kenteken = detail.kenteken,
            isChipped = state.detailIsChipped,
            tune = state.detailTune,
            canChip = ChippedTuneCalculator.canBeChipped(detail),
            onToggle = { onEvent(VehicleDetailEvent.OnToggleDetailChipped) },
        )

        if (my != null && !isMyVehicle) {
            Spacer(Modifier.height(8.dp))
            ChippedVehicleCard(
                caption = "Jouw voertuig",
                kenteken = my.kenteken,
                isChipped = state.myIsChipped,
                tune = state.myTune,
                canChip = ChippedTuneCalculator.canBeChipped(my),
                onToggle = { onEvent(VehicleDetailEvent.OnToggleMyChipped) },
            )
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = PakkieAnimations.contentSizeSpec()),
            ) {
                CompareRow(
                    label = powerCompareLabel(
                        formatter = displayFormatter,
                        prefs = unitPreferences,
                        showStage1 = state.detailIsChipped || state.myIsChipped,
                    ),
                    detailText = formatEffectivePower(detail, state.detailIsChipped, state.detailTune, displayFormatter, unitPreferences),
                    myText = my?.let { formatEffectivePower(it, state.myIsChipped, state.myTune, displayFormatter, unitPreferences) } ?: "—",
                    trend = compareHigherBetter(detailKw, myKw),
                )
                CompareDivider()
                CompareRow(
                    label = "Pk per kilo",
                    detailText = displayFormatter.formatPkPerKilo(detail),
                    myText = my?.let { displayFormatter.formatPkPerKilo(it) } ?: "—",
                    trend = compareHigherBetter(
                        displayFormatter.pkPerKilo(detail),
                        my?.let { displayFormatter.pkPerKilo(it) },
                    ),
                )
                CompareDivider()
                CompareRow(
                    label = displayFormatter.weightLabel(unitPreferences, rijklaar = true),
                    detailText = displayFormatter.formatWeight(detail.massaRijklaarKg, unitPreferences),
                    myText = displayFormatter.formatWeight(my?.massaRijklaarKg, unitPreferences),
                    trend = compareLowerBetter(
                        detail.massaRijklaarKg?.toDouble(),
                        my?.massaRijklaarKg?.toDouble(),
                    ),
                    invertArrow = true,
                )
                CompareDivider()
                CompareRow(
                    label = displayFormatter.weightLabel(unitPreferences, rijklaar = false),
                    detailText = displayFormatter.formatWeight(detail.massaLedigKg, unitPreferences),
                    myText = displayFormatter.formatWeight(my?.massaLedigKg, unitPreferences),
                    trend = compareLowerBetter(
                        detail.massaLedigKg?.toDouble(),
                        my?.massaLedigKg?.toDouble(),
                    ),
                    invertArrow = true,
                )
                CompareDivider()
                CompareRow(
                    label = "Top snelheid",
                    detailText = formatTopSpeed(detail.maximaleConstructiesnelheidKmh),
                    myText = formatTopSpeed(my?.maximaleConstructiesnelheidKmh),
                    trend = compareHigherBetter(
                        detail.maximaleConstructiesnelheidKmh?.toDouble(),
                        my?.maximaleConstructiesnelheidKmh?.toDouble(),
                    ),
                )
                CompareDivider()
                CompareRow(
                    label = "Brandstof",
                    detailText = detail.brandstofOmschrijvingen.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "—",
                    myText = my?.brandstofOmschrijvingen.orEmpty().joinToString(", ").ifBlank { "—" },
                    trend = CompareTrend.Neutral,
                )
                CompareDivider()
                CompareRow(
                    label = "Versnellingsbak",
                    detailText = versnellingsbakDisplayNl(detail.versnellingsbakCode),
                    myText = versnellingsbakDisplayNl(my?.versnellingsbakCode),
                    trend = compareTransmission(detail, my),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isMyVehicle) {
            OutlinedButton(
                onClick = { onEvent(VehicleDetailEvent.OnClearAsMyVehicle) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Niet meer mijn auto")
            }
        } else {
            OutlinedButton(
                onClick = { onEvent(VehicleDetailEvent.OnSetAsMyVehicle) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Dit is mijn auto")
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Klaar")
        }
    }
}

private fun formatTopSpeed(kmh: Int?): String =
    kmh?.let { "$it km/h" } ?: "—"

private fun powerCompareLabel(
    formatter: VehicleDisplayFormatter,
    prefs: UnitPreferences,
    showStage1: Boolean,
): String = if (showStage1) "Vermogen (Stage 1)" else formatter.powerLabel(prefs)

private fun formatEffectivePower(
    info: VehicleLicensePlateInfo,
    isChipped: Boolean,
    tune: ChippedTuneEstimate?,
    formatter: VehicleDisplayFormatter,
    prefs: UnitPreferences,
): String {
    val kw = PakkiePakkieCalculator.effectiveVermogenKw(info, tune, isChipped)
    val formatted = formatter.formatPower(kw, prefs)
    return if (isChipped && tune != null) "$formatted (geschat)" else formatted
}

@Composable
private fun CompareDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

private fun compareHigherBetter(detail: Double?, my: Double?): CompareTrend {
    if (detail == null || my == null) return CompareTrend.Neutral
    return when {
        detail > my -> CompareTrend.DetailBetter
        detail < my -> CompareTrend.DetailWorse
        else -> CompareTrend.Neutral
    }
}

private fun compareLowerBetter(detail: Double?, my: Double?): CompareTrend {
    if (detail == null || my == null) return CompareTrend.Neutral
    return when {
        detail < my -> CompareTrend.DetailBetter
        detail > my -> CompareTrend.DetailWorse
        else -> CompareTrend.Neutral
    }
}

private fun compareTransmission(detail: VehicleLicensePlateInfo, my: VehicleLicensePlateInfo?): CompareTrend {
    if (my == null) return CompareTrend.Neutral
    val d = PakkiePakkieCalculator.transmissionMultiplier(detail.versnellingsbakCode, detail.brandstofOmschrijvingen)
    val m = PakkiePakkieCalculator.transmissionMultiplier(my.versnellingsbakCode, my.brandstofOmschrijvingen)
    return when {
        d > m -> CompareTrend.DetailBetter
        d < m -> CompareTrend.DetailWorse
        else -> CompareTrend.Neutral
    }
}

@Composable
private fun CompareRow(
    label: String,
    detailText: String,
    myText: String,
    trend: CompareTrend,
    modifier: Modifier = Modifier,
    invertArrow: Boolean = false,
) {
    val good = MaterialTheme.colorScheme.primary
    val bad = MaterialTheme.colorScheme.error
    val neutral = MaterialTheme.colorScheme.onSurface
    val (arrow, detailColor) = when (trend) {
        CompareTrend.DetailBetter -> (if (invertArrow) "↓" else "↑") to good
        CompareTrend.DetailWorse -> (if (invertArrow) "↑" else "↓") to bad
        CompareTrend.Neutral -> "" to neutral
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompareValueCell(
                modifier = Modifier.weight(1f),
                caption = "Dit",
                value = detailText,
                valueColor = detailColor,
                suffix = arrow,
            )
            CompareValueCell(
                modifier = Modifier.weight(1f),
                caption = "Jouw",
                value = myText,
                valueColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CompareValueCell(
    caption: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    suffix: String = "",
) {
    Column(modifier = modifier) {
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (suffix.isBlank()) value else "$value $suffix",
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VehicleComparePreviewContent() {
    val formatter = VehicleDisplayFormatter()
    VehicleCompareScaffold(
        kenteken = "PL700K",
        state = VehicleDetailState(
            loading = false,
            detail = VehicleLicensePlateInfo(
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
                maximaleConstructiesnelheidKmh = 210,
            ),
            my = VehicleLicensePlateInfo(
                kenteken = "ABC12D",
                merk = "TOYOTA",
                handelsbenaming = "Yaris",
                massaRijklaarKg = 1150,
                massaLedigKg = 1050,
                cilinderinhoudCc = 1498,
                vermogenMassaRijklaar = 0.09,
                vermogenKw = 85.0,
                brandstofOmschrijvingen = listOf("Benzine"),
                hybridKlasse = null,
                versnellingsbakCode = "A",
                aantalVersnellingen = 8,
                maximaleConstructiesnelheidKmh = 180,
            ),
            myVehicleKenteken = "ABC12D",
            errorMessage = null,
        ),
        unitPreferences = UnitPreferences(),
        displayFormatter = formatter,
        onBack = {},
        onEvent = {},
    )
}

@Preview
@Composable
private fun VehicleCompareScreenLightPreview() {
    PreviewContainer(isDarkTheme = false) {
        VehicleComparePreviewContent()
    }
}

@Preview
@Composable
private fun VehicleCompareScreenDarkPreview() {
    PreviewContainer(isDarkTheme = true) {
        VehicleComparePreviewContent()
    }
}
