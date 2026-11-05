package nl.designlama.pakkiepakkie.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import nl.designlama.pakkiepakkie.datastore.TireType
import nl.designlama.pakkiepakkie.datastore.UnitPreferencesRepository
import nl.designlama.pakkiepakkie.datastore.UserVehicleProfile
import nl.designlama.pakkiepakkie.domain.units.UnitPreferences
import nl.designlama.pakkiepakkie.domain.units.UnitSymbols
import nl.designlama.pakkiepakkie.domain.units.VehicleDisplayFormatter
import nl.designlama.pakkiepakkie.network.chipped.ChippedTuneEstimate
import nl.designlama.pakkiepakkie.network.rdw.PakkiePakkieCalculator
import nl.designlama.pakkiepakkie.network.rdw.RdwTransmissionCodes
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.CircularIconButton
import nl.designlama.pakkiepakkie.ui.components.CompareBarTone
import nl.designlama.pakkiepakkie.ui.components.CompareOutcome
import nl.designlama.pakkiepakkie.ui.components.CompareStatRow
import nl.designlama.pakkiepakkie.ui.components.LicensePlateBadge
import nl.designlama.pakkiepakkie.ui.components.PakkieAnimations
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieText
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import nl.designlama.pakkiepakkie.ui.components.ReviewEditorSheet
import nl.designlama.pakkiepakkie.ui.components.UiSymbols
import nl.designlama.pakkiepakkie.ui.components.UserAvatar
import nl.designlama.pakkiepakkie.ui.components.compareBooleanOutcome
import nl.designlama.pakkiepakkie.ui.components.compareNumericOutcome
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.action_close
import pakkiepakkie.shared.generated.resources.action_set_my_vehicle
import pakkiepakkie.shared.generated.resources.compare_label_chipped
import pakkiepakkie.shared.generated.resources.compare_label_curb_weight
import pakkiepakkie.shared.generated.resources.compare_label_empty_weight
import pakkiepakkie.shared.generated.resources.compare_label_modded_exhaust
import pakkiepakkie.shared.generated.resources.compare_label_nitrous
import pakkiepakkie.shared.generated.resources.compare_label_power
import pakkiepakkie.shared.generated.resources.compare_label_tires
import pakkiepakkie.shared.generated.resources.compare_label_top_speed
import pakkiepakkie.shared.generated.resources.compare_set_my_car_hint
import pakkiepakkie.shared.generated.resources.compare_them_username
import pakkiepakkie.shared.generated.resources.compare_value_chipped_no
import pakkiepakkie.shared.generated.resources.compare_value_chipped_yes
import pakkiepakkie.shared.generated.resources.compare_value_exhaust_no
import pakkiepakkie.shared.generated.resources.compare_value_exhaust_yes
import pakkiepakkie.shared.generated.resources.compare_value_nitrous_no
import pakkiepakkie.shared.generated.resources.compare_value_nitrous_yes
import pakkiepakkie.shared.generated.resources.compare_you_username
import pakkiepakkie.shared.generated.resources.format_power_pk
import pakkiepakkie.shared.generated.resources.home_favorite_added
import pakkiepakkie.shared.generated.resources.home_favorite_removed
import pakkiepakkie.shared.generated.resources.profile_tires_allseason
import pakkiepakkie.shared.generated.resources.profile_tires_summer
import pakkiepakkie.shared.generated.resources.profile_tires_winter
import pakkiepakkie.shared.generated.resources.transmission_automatic
import pakkiepakkie.shared.generated.resources.transmission_cvt
import pakkiepakkie.shared.generated.resources.transmission_dct
import pakkiepakkie.shared.generated.resources.transmission_manual
import pakkiepakkie.shared.generated.resources.unit_speed_kmh

private val BattleCardShape = RoundedCornerShape(28.dp)
private val BattleCardPadding = 16.dp

@Composable
internal fun versnellingsbakDisplayNl(code: String?): String = when (code?.trim()?.uppercase()) {
    RdwTransmissionCodes.AUTOMATIC -> stringResource(Res.string.transmission_automatic)
    RdwTransmissionCodes.MANUAL -> stringResource(Res.string.transmission_manual)
    RdwTransmissionCodes.CVT -> stringResource(Res.string.transmission_cvt)
    RdwTransmissionCodes.DCT_G, RdwTransmissionCodes.DCT_D -> stringResource(Res.string.transmission_dct)
    null, "" -> UnitSymbols.EM_DASH
    else -> code.trim()
}

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
    val snackbarHostState = remember { SnackbarHostState() }
    val favoriteAdded = stringResource(Res.string.home_favorite_added)
    val favoriteRemoved = stringResource(Res.string.home_favorite_removed)

    LaunchedEffect(viewModel) {
        viewModel.snackbar.collectLatest { key ->
            val message = when (key) {
                HomeViewModel.SNACKBAR_ADDED -> favoriteAdded
                HomeViewModel.SNACKBAR_REMOVED -> favoriteRemoved
                else -> key
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    VehicleCompareScaffold(
        kenteken = kenteken,
        state = state,
        unitPreferences = unitPreferences,
        displayFormatter = displayFormatter,
        snackbarHostState = snackbarHostState,
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
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onEvent: (VehicleDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = BattleCardPadding, vertical = BattleCardPadding),
        ) {
            when {
                state.loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PakkiePakkieText(
                            text = state.errorMessage.orEmpty(),
                            textColor = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier.height(16.dp))
                        Button(onClick = onBack) { Text(stringResource(Res.string.action_close)) }
                    }
                }

                state.detail != null -> {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = BattleCardShape,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp,
                        tonalElevation = 0.dp,
                    ) {
                        VehicleCompareContent(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scroll)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            kenteken = kenteken,
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
    }
}

@Composable
private fun VehicleCompareContent(
    kenteken: String,
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
    val isMyVehicle = state.isMyVehicle(detail.kenteken)
    val profile = state.myProfile

    Column(
        modifier = modifier.animateContentSize(
            animationSpec = PakkieAnimations.contentSizeSpec(),
        ),
    ) {
        BattleCardHeader(
            kenteken = kenteken,
            isFavorite = state.isFavorite,
            onBack = onBack,
            onFavoriteClick = { onEvent(VehicleDetailEvent.OnToggleFavorite) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        BattleHeader(
            my = my,
            detail = detail,
            isViewingOwnCar = isMyVehicle,
        )

        if (my == null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.compare_set_my_car_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onEvent(VehicleDetailEvent.OnSetAsMyVehicle) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.action_set_my_vehicle))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        BattleStats(
            my = my,
            detail = detail,
            myKw = myKw,
            detailKw = detailKw,
            myIsChipped = state.myIsChipped,
            detailIsChipped = state.detailIsChipped,
            profile = profile,
            isMyVehicle = isMyVehicle,
            displayFormatter = displayFormatter,
            unitPreferences = unitPreferences,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BattleCardHeader(
    kenteken: String,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularIconButton(symbol = UiSymbols.BACK, onClick = onBack)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            LicensePlateBadge(kenteken = kenteken, compact = true)
        }
        CircularIconButton(
            symbol = if (isFavorite) UiSymbols.STAR_FILLED else UiSymbols.STAR_OUTLINE,
            onClick = onFavoriteClick,
        )
    }
}

@Composable
private fun BattleHeader(
    my: VehicleLicensePlateInfo?,
    detail: VehicleLicensePlateInfo,
    isViewingOwnCar: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BattleProfile(
            modifier = Modifier.weight(1f),
            username = stringResource(Res.string.compare_you_username),
            carName = my?.let { "${it.merk} ${it.handelsbenaming}".trim() }
                ?: stringResource(Res.string.compare_set_my_car_hint),
            avatarLabel = "PP",
            alignEnd = false,
        )
        Text(
            text = UiSymbols.SWORDS,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        BattleProfile(
            modifier = Modifier.weight(1f),
            username = if (isViewingOwnCar) {
                stringResource(Res.string.compare_you_username)
            } else {
                stringResource(Res.string.compare_them_username)
            },
            carName = "${detail.merk} ${detail.handelsbenaming}".trim(),
            avatarLabel = detail.merk.take(2).ifBlank { "??" },
            alignEnd = true,
        )
    }
}

@Composable
private fun BattleProfile(
    username: String,
    carName: String,
    avatarLabel: String,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
        ) {
            if (!alignEnd) {
                UserAvatar(label = avatarLabel, size = 48.dp)
                Spacer(modifier = Modifier.size(8.dp))
            }
            Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = carName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
                )
            }
            if (alignEnd) {
                Spacer(modifier = Modifier.size(8.dp))
                UserAvatar(label = avatarLabel, size = 48.dp)
            }
        }
    }
}

@Composable
private fun BattleStats(
    my: VehicleLicensePlateInfo?,
    detail: VehicleLicensePlateInfo,
    myKw: Double?,
    detailKw: Double?,
    myIsChipped: Boolean,
    detailIsChipped: Boolean,
    profile: UserVehicleProfile,
    isMyVehicle: Boolean,
    displayFormatter: VehicleDisplayFormatter,
    unitPreferences: UnitPreferences,
) {
    val myPk = myKw?.times(1.36)
    val detailPk = detailKw?.times(1.36)
    val myPowerText = myPk?.let { stringResource(Res.string.format_power_pk, it.toInt()) }
        ?: UnitSymbols.EM_DASH
    val detailPowerText = detailPk?.let { stringResource(Res.string.format_power_pk, it.toInt()) }
        ?: UnitSymbols.EM_DASH

    Column(modifier = Modifier.fillMaxWidth()) {
        NumericStat(
            label = stringResource(Res.string.compare_label_power),
            icon = "⚡",
            leftValue = myPowerText,
            rightValue = detailPowerText,
            leftNumeric = myPk,
            rightNumeric = detailPk,
            higherIsBetter = true,
        )
        NumericStat(
            label = stringResource(Res.string.compare_label_top_speed),
            icon = "🏁",
            leftValue = formatTopSpeed(my?.maximaleConstructiesnelheidKmh),
            rightValue = formatTopSpeed(detail.maximaleConstructiesnelheidKmh),
            leftNumeric = my?.maximaleConstructiesnelheidKmh?.toDouble(),
            rightNumeric = detail.maximaleConstructiesnelheidKmh?.toDouble(),
            higherIsBetter = true,
        )
        NumericStat(
            label = stringResource(Res.string.compare_label_curb_weight),
            icon = "⚖",
            leftValue = displayFormatter.formatWeight(my?.massaRijklaarKg, unitPreferences),
            rightValue = displayFormatter.formatWeight(detail.massaRijklaarKg, unitPreferences),
            leftNumeric = my?.massaRijklaarKg?.toDouble(),
            rightNumeric = detail.massaRijklaarKg?.toDouble(),
            higherIsBetter = false,
        )
        NumericStat(
            label = stringResource(Res.string.compare_label_empty_weight),
            icon = "⚖",
            leftValue = displayFormatter.formatWeight(my?.massaLedigKg, unitPreferences),
            rightValue = displayFormatter.formatWeight(detail.massaLedigKg, unitPreferences),
            leftNumeric = my?.massaLedigKg?.toDouble(),
            rightNumeric = detail.massaLedigKg?.toDouble(),
            higherIsBetter = false,
        )

        val leftChipped = my != null && myIsChipped
        val rightChipped = detailIsChipped
        BooleanStat(
            label = stringResource(Res.string.compare_label_chipped),
            icon = "▣",
            leftValue = when {
                my == null -> UnitSymbols.EM_DASH
                leftChipped -> stringResource(Res.string.compare_value_chipped_yes)
                else -> stringResource(Res.string.compare_value_chipped_no)
            },
            rightValue = if (rightChipped) {
                stringResource(Res.string.compare_value_chipped_yes)
            } else {
                stringResource(Res.string.compare_value_chipped_no)
            },
            leftWins = leftChipped && !rightChipped,
            tie = leftChipped == rightChipped,
        )

        val leftNitrous = my != null && profile.hasNitrous
        val rightNitrous = isMyVehicle && profile.hasNitrous
        BooleanStat(
            label = stringResource(Res.string.compare_label_nitrous),
            icon = "⬡",
            leftValue = when {
                my == null -> UnitSymbols.EM_DASH
                leftNitrous -> stringResource(Res.string.compare_value_nitrous_yes)
                else -> stringResource(Res.string.compare_value_nitrous_no)
            },
            rightValue = if (rightNitrous) {
                stringResource(Res.string.compare_value_nitrous_yes)
            } else {
                stringResource(Res.string.compare_value_nitrous_no)
            },
            leftWins = leftNitrous && !rightNitrous,
            tie = leftNitrous == rightNitrous,
        )

        val leftExhaust = my != null && profile.hasModdedExhaust
        val rightExhaust = isMyVehicle && profile.hasModdedExhaust
        BooleanStat(
            label = stringResource(Res.string.compare_label_modded_exhaust),
            icon = "⌁",
            leftValue = when {
                my == null -> UnitSymbols.EM_DASH
                leftExhaust -> stringResource(Res.string.compare_value_exhaust_yes)
                else -> stringResource(Res.string.compare_value_exhaust_no)
            },
            rightValue = if (rightExhaust) {
                stringResource(Res.string.compare_value_exhaust_yes)
            } else {
                stringResource(Res.string.compare_value_exhaust_no)
            },
            leftWins = leftExhaust && !rightExhaust,
            tie = leftExhaust == rightExhaust,
        )

        CompareStatRow(
            label = stringResource(Res.string.compare_label_tires),
            leftValue = if (my == null) UnitSymbols.EM_DASH else tireLabel(profile.tires),
            rightValue = if (isMyVehicle) tireLabel(profile.tires) else UnitSymbols.EM_DASH,
            outcome = CompareOutcome(CompareBarTone.Tie, 1f, leftWins = false, isTie = true),
            icon = "◉",
        )
    }
}

@Composable
private fun NumericStat(
    label: String,
    leftValue: String,
    rightValue: String,
    leftNumeric: Double?,
    rightNumeric: Double?,
    higherIsBetter: Boolean,
    icon: String? = null,
) {
    CompareStatRow(
        label = label,
        leftValue = leftValue,
        rightValue = rightValue,
        outcome = compareNumericOutcome(leftNumeric, rightNumeric, higherIsBetter),
        icon = icon,
    )
}

@Composable
private fun BooleanStat(
    label: String,
    leftValue: String,
    rightValue: String,
    leftWins: Boolean,
    tie: Boolean,
    icon: String? = null,
) {
    CompareStatRow(
        label = label,
        leftValue = leftValue,
        rightValue = rightValue,
        outcome = compareBooleanOutcome(leftWins = leftWins, tie = tie),
        icon = icon,
    )
}

@Composable
private fun tireLabel(tires: TireType): String = when (tires) {
    TireType.Summer -> stringResource(Res.string.profile_tires_summer)
    TireType.Winter -> stringResource(Res.string.profile_tires_winter)
    TireType.AllSeason -> stringResource(Res.string.profile_tires_allseason)
}

@Composable
private fun formatTopSpeed(kmh: Int?): String =
    kmh?.let { stringResource(Res.string.unit_speed_kmh, it) } ?: UnitSymbols.EM_DASH

@Composable
private fun VehicleComparePreviewContent() {
    val formatter = VehicleDisplayFormatter()
    VehicleCompareScaffold(
        kenteken = "78DFW9",
        state = VehicleDetailState(
            loading = false,
            detail = VehicleLicensePlateInfo(
                kenteken = "78DFW9",
                merk = "Volkswagen",
                handelsbenaming = "Golf 1.0 TSI",
                massaRijklaarKg = 1420,
                massaLedigKg = 1320,
                cilinderinhoudCc = 999,
                vermogenMassaRijklaar = 0.055,
                vermogenKw = 77.0,
                brandstofOmschrijvingen = listOf("Benzine"),
                hybridKlasse = null,
                versnellingsbakCode = "M",
                aantalVersnellingen = 6,
                maximaleConstructiesnelheidKmh = 160,
            ),
            my = VehicleLicensePlateInfo(
                kenteken = "HDJ70K",
                merk = "BMW",
                handelsbenaming = "1er Reihe",
                massaRijklaarKg = 1370,
                massaLedigKg = 1270,
                cilinderinhoudCc = 1499,
                vermogenMassaRijklaar = 0.037,
                vermogenKw = 51.0,
                brandstofOmschrijvingen = listOf("Benzine"),
                hybridKlasse = null,
                versnellingsbakCode = "A",
                aantalVersnellingen = 8,
                maximaleConstructiesnelheidKmh = 420,
            ),
            myVehicleKenteken = "HDJ70K",
            myIsChipped = true,
            detailIsChipped = false,
            myProfile = UserVehicleProfile(hasNitrous = true, hasModdedExhaust = false),
            errorMessage = null,
        ),
        unitPreferences = UnitPreferences(),
        displayFormatter = formatter,
        snackbarHostState = remember { SnackbarHostState() },
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
