package nl.designlama.pakkiepakkie.ui

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.designlama.pakkiepakkie.datastore.UnitPreferencesRepository
import nl.designlama.pakkiepakkie.domain.units.UnitPreferences
import nl.designlama.pakkiepakkie.domain.units.VehicleDisplayFormatter
import nl.designlama.pakkiepakkie.network.rdw.PakkiePakkieCalculator
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieGauge
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieText
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
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
    modifier: Modifier = Modifier,
) {

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        CenterAlignedTopAppBar(
            title = { Text(text = formatLicensePlate(kenteken)) },
            navigationIcon = {
                TextButton(onClick = onBack) { Text("Terug") }
            },
        )

        when {
            state.loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                val err = state.errorMessage.orEmpty()
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PakkiePakkieText(text = err, textColor = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Sluiten") }
                }
            }

            state.detail != null -> {
                VehicleCompareContent(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    detail = state.detail!!,
                    my = state.my,
                    unitPreferences = unitPreferences,
                    displayFormatter = displayFormatter,
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun VehicleCompareContent(
    detail: VehicleLicensePlateInfo,
    my: VehicleLicensePlateInfo?,
    unitPreferences: UnitPreferences,
    displayFormatter: VehicleDisplayFormatter,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val winPct = my?.let { PakkiePakkieCalculator.winProbabilityPercent(it, detail) }
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        PakkiePakkieText(
            text = "Jouw kans om te winnen vs dit voertuig",
            textColor = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        PakkiePakkieGauge(percent = winPct, sizeDp = 140f)
        if (my == null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Stel je auto in via de startpagina om een percentage te zien.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
    Spacer(Modifier.height(24.dp))
    CompareRow(
        label = displayFormatter.powerLabel(unitPreferences),
        detailText = displayFormatter.formatPower(detail.vermogenKw, unitPreferences),
        myText = displayFormatter.formatPower(my?.vermogenKw, unitPreferences),
        trend = compareHigherBetter(detail.vermogenKw, my?.vermogenKw),
    )
    CompareRow(
        label = "Pk per kilo",
        detailText = displayFormatter.formatPkPerKilo(detail),
        myText = my?.let { displayFormatter.formatPkPerKilo(it) } ?: "—",
        trend = compareHigherBetter(
            displayFormatter.pkPerKilo(detail),
            my?.let { displayFormatter.pkPerKilo(it) },
        ),
    )
    CompareRow(
        label = displayFormatter.weightLabel(unitPreferences, rijklaar = true),
        detailText = displayFormatter.formatWeight(detail.massaRijklaarKg, unitPreferences),
        myText = displayFormatter.formatWeight(my?.massaRijklaarKg, unitPreferences),
        trend = compareLowerBetter(
            detail.massaRijklaarKg?.toDouble(),
            my?.massaRijklaarKg?.toDouble(),
        ),
    )
    CompareRow(
        label = displayFormatter.weightLabel(unitPreferences, rijklaar = false),
        detailText = displayFormatter.formatWeight(detail.massaLedigKg, unitPreferences),
        myText = displayFormatter.formatWeight(my?.massaLedigKg, unitPreferences),
        trend = compareLowerBetter(
            detail.massaLedigKg?.toDouble(),
            my?.massaLedigKg?.toDouble(),
        ),
    )
    CompareRow(
        label = "Brandstof",
        detailText = detail.brandstofOmschrijvingen.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "—",
        myText = my?.brandstofOmschrijvingen.orEmpty().joinToString(", ").ifBlank { "—" },
        trend = CompareTrend.Neutral,
    )
    CompareRow(
        label = "Versnellingsbak",
        detailText = versnellingsbakDisplayNl(detail.versnellingsbakCode),
        myText = versnellingsbakDisplayNl(my?.versnellingsbakCode),
        trend = compareTransmission(detail, my),
    )
    Spacer(Modifier.height(24.dp))
    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Text("Klaar")
    }
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
) {
    val good = MaterialTheme.colorScheme.primary
    val bad = MaterialTheme.colorScheme.error
    val neutral = MaterialTheme.colorScheme.onSurfaceVariant
    val (arrow, detailColor) = when (trend) {
        CompareTrend.DetailBetter -> "↑" to good
        CompareTrend.DetailWorse -> "↓" to bad
        CompareTrend.Neutral -> "" to neutral
    }
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        PakkiePakkieText(text = label, textColor = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Dit: $detailText $arrow",
                style = MaterialTheme.typography.bodyLarge,
                color = detailColor,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Jouw: $myText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
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
            ),
            errorMessage = null,
        ),
        unitPreferences = UnitPreferences(),
        displayFormatter = formatter,
        onBack = {},
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
