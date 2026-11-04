package nl.designlama.pakkiepakkie.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import nl.designlama.pakkiepakkie.data.LatestReviewedPlate
import nl.designlama.pakkiepakkie.data.local.VehicleLookupEntity
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.DutchLicensePlateInput
import nl.designlama.pakkiepakkie.ui.components.LicensePlateInputStyle
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieGauge
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieText
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import nl.designlama.pakkiepakkie.ui.components.StarRating
import nl.designlama.pakkiepakkie.ui.components.UiSymbols
import nl.designlama.pakkiepakkie.ui.components.formatLicensePlate
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.home_gauge_average
import pakkiepakkie.shared.generated.resources.home_no_my_vehicle_body
import pakkiepakkie.shared.generated.resources.home_no_my_vehicle_title
import pakkiepakkie.shared.generated.resources.home_profile_placeholder
import pakkiepakkie.shared.generated.resources.home_section_latest_reviewed
import pakkiepakkie.shared.generated.resources.home_section_recent
import pakkiepakkie.shared.generated.resources.review_average_summary
import pakkiepakkie.shared.generated.resources.review_count_one
import pakkiepakkie.shared.generated.resources.review_count_other
import kotlin.math.max
import kotlin.math.min

private val HeroMaxHeight = 236.dp
private val SheetCorner = 28.dp
private const val SearchDockProgress = 0.88f

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
    val density = LocalDensity.current
    val collapseRangePx = with(density) { HeroMaxHeight.toPx() }
    var collapsePx by remember { mutableFloatStateOf(0f) }
    val collapseProgress = (collapsePx / collapseRangePx).coerceIn(0f, 1f)
    val searchInTopBar = collapseProgress >= SearchDockProgress
    val listState = rememberLazyListState()
    val listAtTop = remember { mutableStateOf(true) }
    listAtTop.value =
        listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0

    val nestedScrollConnection = remember(collapseRangePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0f && collapsePx < collapseRangePx) {
                    val consumed = min(-delta, collapseRangePx - collapsePx)
                    collapsePx += consumed
                    return Offset(0f, -consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val delta = available.y
                if (delta > 0f && collapsePx > 0f && listAtTop.value) {
                    val consumedUp = min(delta, collapsePx)
                    collapsePx -= consumedUp
                    return Offset(0f, consumedUp)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y > 0f && collapsePx > 0f && listAtTop.value) {
                    collapsePx = 0f
                    return Velocity(0f, available.y)
                }
                return Velocity.Zero
            }
        }
    }

    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .nestedScroll(nestedScrollConnection),
    ) {
        HomeTopBar(
            searchInTopBar = searchInTopBar,
            licensePlateInput = state.licensePlateInput,
            searchEnabled = !state.loading,
            onLicensePlateChange = { onEvent(HomeEvent.OnLicensePlateChange(it)) },
            onSearch = { onEvent(HomeEvent.OnSearchClick) },
            onSettingsClick = { onEvent(HomeEvent.OnSettingsClick) },
        )

        val heroHeight = HeroMaxHeight * (1f - collapseProgress)
        if (heroHeight > 0.dp) {
            HomeHero(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .graphicsLayer {
                        alpha = max(0f, 1f - collapseProgress * 1.35f)
                        translationY = -collapsePx * 0.15f
                    },
            )
        }

        if (!searchInTopBar) {
            DutchLicensePlateInput(
                value = state.licensePlateInput,
                onValueChange = { raw, _ -> onEvent(HomeEvent.OnLicensePlateChange(raw)) },
                showClearButton = true,
                style = LicensePlateInputStyle.Search,
                onDone = { if (state.canSearch) onEvent(HomeEvent.OnSearchClick) },
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
            )
        }

        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
                    .size(28.dp),
                strokeWidth = 3.dp,
            )
        }

        state.errorMessage?.let { msg ->
            PakkiePakkieText(
                text = msg,
                textColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = SheetCorner, topEnd = SheetCorner),
            tonalElevation = 1.dp,
            shadowElevation = 8.dp,
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = 24.dp + navigationBarPadding.calculateBottomPadding(),
                ),
            ) {
                if (state.recent.isNotEmpty()) {
                    item(key = "recent-header") {
                        SheetSectionHeader(stringResource(Res.string.home_section_recent))
                    }
                    items(state.recent, key = { "recent-${it.kenteken}" }) { row ->
                        RecentVehicleRow(
                            row = row,
                            winPercent = state.recentWinPercent[row.kenteken],
                            isMyVehicle = isSameKenteken(row.kenteken, state.myVehicleKenteken),
                            onClick = { onEvent(HomeEvent.OnRecentRowClick(row.kenteken)) },
                            onSetMyVehicle = { onEvent(HomeEvent.OnRecentSetMyVehicle(row.kenteken)) },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }

                if (state.latestReviewed.isNotEmpty()) {
                    item(key = "reviewed-header") {
                        SheetSectionHeader(stringResource(Res.string.home_section_latest_reviewed))
                    }
                    items(state.latestReviewed, key = { "rev-${it.kenteken}" }) { row ->
                        LatestReviewedRow(
                            row = row,
                            onClick = { onEvent(HomeEvent.OnLatestReviewedClick(row.kenteken)) },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    searchInTopBar: Boolean,
    licensePlateInput: String,
    searchEnabled: Boolean,
    onLicensePlateChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.home_profile_placeholder),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (searchInTopBar) {
            DutchLicensePlateInput(
                value = licensePlateInput,
                onValueChange = { raw, _ -> onLicensePlateChange(raw) },
                showClearButton = true,
                style = LicensePlateInputStyle.Search,
                compact = true,
                onDone = onSearch,
                enabled = searchEnabled,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        IconButton(onClick = onSettingsClick) {
            Text(
                text = UiSymbols.SETTINGS,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HomeHero(
    state: HomeState,
    modifier: Modifier = Modifier,
) {
    val myInfo = state.myVehicleInfo
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PakkiePakkieGauge(
            percent = state.myVehicleAverageWinPercent,
            sizeDp = 148f,
            showLabel = false,
            subtitle = stringResource(Res.string.home_gauge_average),
            animationEnabled = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (myInfo != null) {
            Text(
                text = formatLicensePlate(myInfo.kenteken),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "${myInfo.merk} ${myInfo.handelsbenaming}".trim(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                text = stringResource(Res.string.home_no_my_vehicle_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(Res.string.home_no_my_vehicle_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SheetSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun LatestReviewedRow(
    row: LatestReviewedPlate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatLicensePlate(row.kenteken),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            val countLabel = if (row.reviewCount == 1) {
                stringResource(Res.string.review_count_one, row.reviewCount)
            } else {
                stringResource(Res.string.review_count_other, row.reviewCount)
            }
            val subtitle = stringResource(
                Res.string.review_average_summary,
                formatHomeAverageRating(row.averageRating),
                countLabel,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            row.latestText?.takeIf { it.isNotBlank() }?.let { snippet ->
                Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        StarRating(
            rating = row.averageRating.toInt().coerceIn(1, 5),
            starSizeSp = 18f,
        )
    }
}

private fun formatHomeAverageRating(average: Float): String {
    val tenths = ((average * 10f) + 0.5f).toInt()
    val whole = tenths / 10
    val fraction = tenths % 10
    return if (fraction == 0) "$whole" else "$whole,$fraction"
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
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PakkiePakkieGauge(
            percent = winPercent,
            sizeDp = 48f,
            showLabel = false,
            animationEnabled = false,
        )
        Column(modifier = Modifier.weight(1f)) {
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
        IconButton(onClick = onSetMyVehicle) {
            Text(
                text = if (isMyVehicle) UiSymbols.STAR_FILLED else UiSymbols.STAR_OUTLINE,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(if (isMyVehicle) 1f else 0.7f),
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
                VehicleLookupEntity(
                    kenteken = "56ZKD3",
                    merk = "OPEL",
                    handelsbenaming = "Corsa",
                    massaRijklaarKg = 1200,
                    vermogenKw = 66.0,
                    vermogenPk = 90.0,
                    massaLedigKg = 1100,
                    cilinderinhoudCc = 1200,
                    vermogenMassaRijklaar = 0.055,
                    primaryBrandstof = "Benzine",
                    hybridKlasse = null,
                    brandstofJson = "[\"Benzine\"]",
                    versnellingsbakCode = "M",
                    aantalVersnellingen = 5,
                    dataVersion = 2,
                    lastViewedAt = 0L,
                    lastFetchedAt = 0L,
                    maximaleConstructiesnelheidKmh = 180,
                    isChipped = false,
                ),
            ),
            myVehicleKenteken = "PL700K",
            myVehicleInfo = VehicleLicensePlateInfo(
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
            recentWinPercent = mapOf("PL700K" to 55f, "56ZKD3" to 75f),
            myVehicleAverageWinPercent = 55f,
            errorMessage = null,
        ),
        onEvent = {},
    )
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    PreviewContainer {
        PreviewContent()
    }
}
