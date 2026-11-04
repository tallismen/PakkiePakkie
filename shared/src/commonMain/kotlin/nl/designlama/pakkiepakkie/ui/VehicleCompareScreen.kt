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
import nl.designlama.pakkiepakkie.domain.units.UnitSymbols
import nl.designlama.pakkiepakkie.domain.units.VehicleDisplayFormatter
import nl.designlama.pakkiepakkie.network.chipped.ChippedTuneCalculator
import nl.designlama.pakkiepakkie.network.chipped.ChippedTuneEstimate
import nl.designlama.pakkiepakkie.network.rdw.PakkiePakkieCalculator
import nl.designlama.pakkiepakkie.network.rdw.RdwTransmissionCodes
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieGauge
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieText
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieTopBar
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import nl.designlama.pakkiepakkie.ui.components.ChippedKentekenTitle
import nl.designlama.pakkiepakkie.ui.components.ChippedVehicleCard
import nl.designlama.pakkiepakkie.ui.components.ReviewEditorSheet
import nl.designlama.pakkiepakkie.ui.components.ReviewListItem
import nl.designlama.pakkiepakkie.ui.components.StarRating
import androidx.compose.ui.tooling.preview.PreviewLightDark
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.action_clear_my_vehicle
import pakkiepakkie.shared.generated.resources.action_close
import pakkiepakkie.shared.generated.resources.action_done
import pakkiepakkie.shared.generated.resources.action_edit_review
import pakkiepakkie.shared.generated.resources.action_set_my_vehicle
import pakkiepakkie.shared.generated.resources.action_write_review
import pakkiepakkie.shared.generated.resources.compare_caption_this_short
import pakkiepakkie.shared.generated.resources.compare_caption_this_vehicle
import pakkiepakkie.shared.generated.resources.compare_caption_your_short
import pakkiepakkie.shared.generated.resources.compare_caption_your_vehicle
import pakkiepakkie.shared.generated.resources.compare_label_fuel
import pakkiepakkie.shared.generated.resources.compare_label_pk_per_kg
import pakkiepakkie.shared.generated.resources.compare_label_power_stage1
import pakkiepakkie.shared.generated.resources.compare_label_top_speed
import pakkiepakkie.shared.generated.resources.compare_label_transmission
import pakkiepakkie.shared.generated.resources.compare_power_estimated_suffix
import pakkiepakkie.shared.generated.resources.compare_set_my_car_hint
import pakkiepakkie.shared.generated.resources.compare_win_chance_disclaimer
import pakkiepakkie.shared.generated.resources.compare_win_chance_title
import pakkiepakkie.shared.generated.resources.review_average_summary
import pakkiepakkie.shared.generated.resources.review_count_one
import pakkiepakkie.shared.generated.resources.review_count_other
import pakkiepakkie.shared.generated.resources.reviews_empty
import pakkiepakkie.shared.generated.resources.reviews_section_title
import pakkiepakkie.shared.generated.resources.transmission_automatic
import pakkiepakkie.shared.generated.resources.transmission_cvt
import pakkiepakkie.shared.generated.resources.transmission_dct
import pakkiepakkie.shared.generated.resources.transmission_manual
import pakkiepakkie.shared.generated.resources.unit_speed_kmh

@Composable
internal fun versnellingsbakDisplayNl(code: String?): String = when (code?.trim()?.uppercase()) {
    RdwTransmissionCodes.AUTOMATIC -> stringResource(Res.string.transmission_automatic)
    RdwTransmissionCodes.MANUAL -> stringResource(Res.string.transmission_manual)
    RdwTransmissionCodes.CVT -> stringResource(Res.string.transmission_cvt)
    RdwTransmissionCodes.DCT_G, RdwTransmissionCodes.DCT_D -> stringResource(Res.string.transmission_dct)
    null, "" -> UnitSymbols.EM_DASH
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
    if (state.reviewSheetVisible) {
        ReviewEditorSheet(
            kenteken = kenteken,
            rating = state.draftRating,
            text = state.draftText,
            isEditing = state.myReview != null,
            submitting = state.reviewSubmitting,
            errorMessage = state.reviewErrorMessage,
            onRatingChange = { viewModel.onEvent(VehicleDetailEvent.OnDraftRatingChange(it)) },
            onTextChange = { viewModel.onEvent(VehicleDetailEvent.OnDraftTextChange(it)) },
            onSubmit = { viewModel.onEvent(VehicleDetailEvent.OnSubmitReview) },
            onDismiss = { viewModel.onEvent(VehicleDetailEvent.OnDismissReviewSheet) },
        )
    }
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
                    Button(onClick = onBack) { Text(stringResource(Res.string.action_close)) }
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
                    text = stringResource(Res.string.compare_win_chance_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                PakkiePakkieGauge(percent = winPct, sizeDp = 140f)
                if (my != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(Res.string.compare_win_chance_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                if (my == null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.compare_set_my_car_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        ChippedVehicleCard(
            caption = stringResource(Res.string.compare_caption_this_vehicle),
            kenteken = detail.kenteken,
            isChipped = state.detailIsChipped,
            tune = state.detailTune,
            canChip = ChippedTuneCalculator.canBeChipped(detail),
            onToggle = { onEvent(VehicleDetailEvent.OnToggleDetailChipped) },
        )

        if (my != null && !isMyVehicle) {
            Spacer(Modifier.height(8.dp))
            ChippedVehicleCard(
                caption = stringResource(Res.string.compare_caption_your_vehicle),
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
                        prefs = unitPreferences,
                        showStage1 = state.detailIsChipped || state.myIsChipped,
                    ),
                    detailText = formatEffectivePower(detail, state.detailIsChipped, state.detailTune, displayFormatter, unitPreferences),
                    myText = my?.let { formatEffectivePower(it, state.myIsChipped, state.myTune, displayFormatter, unitPreferences) }
                        ?: UnitSymbols.EM_DASH,
                    trend = compareHigherBetter(detailKw, myKw),
                )
                CompareDivider()
                CompareRow(
                    label = stringResource(Res.string.compare_label_pk_per_kg),
                    detailText = displayFormatter.formatPkPerKilo(detail),
                    myText = my?.let { displayFormatter.formatPkPerKilo(it) } ?: UnitSymbols.EM_DASH,
                    trend = compareHigherBetter(
                        displayFormatter.pkPerKilo(detail),
                        my?.let { displayFormatter.pkPerKilo(it) },
                    ),
                )
                CompareDivider()
                CompareRow(
                    label = weightSpecLabel(unitPreferences, rijklaar = true),
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
                    label = weightSpecLabel(unitPreferences, rijklaar = false),
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
                    label = stringResource(Res.string.compare_label_top_speed),
                    detailText = formatTopSpeed(detail.maximaleConstructiesnelheidKmh),
                    myText = formatTopSpeed(my?.maximaleConstructiesnelheidKmh),
                    trend = compareHigherBetter(
                        detail.maximaleConstructiesnelheidKmh?.toDouble(),
                        my?.maximaleConstructiesnelheidKmh?.toDouble(),
                    ),
                )
                CompareDivider()
                CompareRow(
                    label = stringResource(Res.string.compare_label_fuel),
                    detailText = detail.brandstofOmschrijvingen.takeIf { it.isNotEmpty() }?.joinToString(", ")
                        ?: UnitSymbols.EM_DASH,
                    myText = my?.brandstofOmschrijvingen.orEmpty().joinToString(", ").ifBlank { UnitSymbols.EM_DASH },
                    trend = CompareTrend.Neutral,
                )
                CompareDivider()
                CompareRow(
                    label = stringResource(Res.string.compare_label_transmission),
                    detailText = versnellingsbakDisplayNl(detail.versnellingsbakCode),
                    myText = versnellingsbakDisplayNl(my?.versnellingsbakCode),
                    trend = compareTransmission(detail, my),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        ReviewsSection(
            state = state,
            onEvent = onEvent,
        )

        Spacer(Modifier.height(16.dp))

        if (isMyVehicle) {
            OutlinedButton(
                onClick = { onEvent(VehicleDetailEvent.OnClearAsMyVehicle) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.action_clear_my_vehicle))
            }
        } else {
            OutlinedButton(
                onClick = { onEvent(VehicleDetailEvent.OnSetAsMyVehicle) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.action_set_my_vehicle))
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.action_done))
        }
    }
}

@Composable
private fun ReviewsSection(
    state: VehicleDetailState,
    onEvent: (VehicleDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.reviews_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))

            val average = state.averageRating
            if (average != null && state.reviews.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StarRating(rating = average.toInt().coerceIn(1, 5), starSizeSp = 18f)
                    val countLabel = if (state.reviews.size == 1) {
                        stringResource(Res.string.review_count_one, state.reviews.size)
                    } else {
                        stringResource(Res.string.review_count_other, state.reviews.size)
                    }
                    Text(
                        text = stringResource(
                            Res.string.review_average_summary,
                            formatAverageRating(average),
                            countLabel,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text(
                    text = stringResource(Res.string.reviews_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            state.reviews.forEachIndexed { index, review ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                ReviewListItem(review = review)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onEvent(VehicleDetailEvent.OnOpenReviewSheet) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (state.myReview != null) {
                        stringResource(Res.string.action_edit_review)
                    } else {
                        stringResource(Res.string.action_write_review)
                    },
                )
            }
        }
    }
}

private fun formatAverageRating(average: Float): String {
    val tenths = ((average * 10f) + 0.5f).toInt()
    val whole = tenths / 10
    val fraction = tenths % 10
    return if (fraction == 0) "$whole" else "$whole,$fraction"
}

@Composable
private fun formatTopSpeed(kmh: Int?): String =
    kmh?.let { stringResource(Res.string.unit_speed_kmh, it) } ?: UnitSymbols.EM_DASH

@Composable
private fun powerCompareLabel(
    prefs: UnitPreferences,
    showStage1: Boolean,
): String = if (showStage1) {
    stringResource(Res.string.compare_label_power_stage1)
} else {
    powerSpecLabel(prefs)
}

@Composable
private fun formatEffectivePower(
    info: VehicleLicensePlateInfo,
    isChipped: Boolean,
    tune: ChippedTuneEstimate?,
    formatter: VehicleDisplayFormatter,
    prefs: UnitPreferences,
): String {
    val kw = PakkiePakkieCalculator.effectiveVermogenKw(info, tune, isChipped)
    val formatted = formatter.formatPower(kw, prefs)
    return if (isChipped && tune != null) {
        formatted + stringResource(Res.string.compare_power_estimated_suffix)
    } else {
        formatted
    }
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
                caption = stringResource(Res.string.compare_caption_this_short),
                value = detailText,
                valueColor = detailColor,
                suffix = arrow,
            )
            CompareValueCell(
                modifier = Modifier.weight(1f),
                caption = stringResource(Res.string.compare_caption_your_short),
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

@PreviewLightDark
@Composable
private fun VehicleCompareScreenPreview() {
    PreviewContainer {
        VehicleComparePreviewContent()
    }
}
