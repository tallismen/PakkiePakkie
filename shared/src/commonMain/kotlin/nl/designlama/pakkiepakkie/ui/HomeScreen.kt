package nl.designlama.pakkiepakkie.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import nl.designlama.pakkiepakkie.data.local.VehicleLookupEntity
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.BrandMark
import nl.designlama.pakkiepakkie.ui.components.FilterIconButton
import nl.designlama.pakkiepakkie.ui.components.LicensePlateBadge
import nl.designlama.pakkiepakkie.ui.components.PakkiePakkieGauge
import nl.designlama.pakkiepakkie.ui.components.PreviewContainer
import nl.designlama.pakkiepakkie.ui.components.SearchEntryField
import nl.designlama.pakkiepakkie.ui.components.UserAvatar
import nl.designlama.pakkiepakkie.ui.components.VehicleListCard
import androidx.compose.ui.tooling.preview.PreviewLightDark
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pakkiepakkie.shared.generated.resources.Res
import pakkiepakkie.shared.generated.resources.format_power_pk
import pakkiepakkie.shared.generated.resources.home_empty_search_prompt
import pakkiepakkie.shared.generated.resources.home_favorite_added
import pakkiepakkie.shared.generated.resources.home_favorite_removed
import pakkiepakkie.shared.generated.resources.home_gauge_average
import pakkiepakkie.shared.generated.resources.home_profile_placeholder
import pakkiepakkie.shared.generated.resources.home_section_favorites
import pakkiepakkie.shared.generated.resources.home_section_recent
import pakkiepakkie.shared.generated.resources.home_vehicle_subtitle
import kotlin.math.max
import kotlin.math.min

private val HeroMaxHeight = 236.dp
private const val SearchDockProgress = 0.75f
private const val HeroMinScale = 0.25f

@Composable
fun HomeScreen(
    onOpenVehicleDetail: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val favoriteAdded = stringResource(Res.string.home_favorite_added)
    val favoriteRemoved = stringResource(Res.string.home_favorite_removed)

    LaunchedEffect(viewModel) {
        viewModel.directions.collectLatest { dir ->
            when (dir) {
                is HomeDirections.OpenVehicleDetail -> onOpenVehicleDetail(dir.kenteken)
                HomeDirections.OpenSearch -> onOpenSearch()
                HomeDirections.OpenProfile -> onOpenProfile()
            }
        }
    }
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        HomeContent(
            state = state,
            onEvent = viewModel::onEvent,
            listModels = { entities, winMap ->
                viewModel.toListModels(entities, winMap, state.favoriteKentekens)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    listModels: (
        List<VehicleLookupEntity>,
        Map<String, Float?>,
    ) -> List<nl.designlama.pakkiepakkie.ui.components.VehicleListItemModel>,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val collapseRangePx = with(density) { HeroMaxHeight.toPx() }
    var collapsePx by remember { mutableFloatStateOf(0f) }
    val collapseProgress = if (state.hasMyVehicle) {
        (collapsePx / collapseRangePx).coerceIn(0f, 1f)
    } else {
        1f
    }
    val searchInTopBar = !state.hasMyVehicle || collapseProgress >= SearchDockProgress
    val heroScale = max(HeroMinScale, 1f - collapseProgress * (1f - HeroMinScale))
    val heroVisible = state.hasMyVehicle && collapseProgress < 0.98f

    val pages = buildList {
        if (state.recent.isNotEmpty()) add(HomePagerPage.Recent)
        if (state.favorites.isNotEmpty()) add(HomePagerPage.Favorites)
    }
    val pagerState = rememberPagerState(pageCount = { pages.size.coerceAtLeast(1) })
    val recentListState = rememberLazyListState()
    val favoritesListState = rememberLazyListState()
    val activeListState = when (pages.getOrNull(pagerState.currentPage)) {
        HomePagerPage.Favorites -> favoritesListState
        else -> recentListState
    }
    val listAtTop = remember { mutableStateOf(true) }
    listAtTop.value =
        activeListState.firstVisibleItemIndex == 0 && activeListState.firstVisibleItemScrollOffset == 0

    val nestedScrollConnection = remember(collapseRangePx, state.hasMyVehicle) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!state.hasMyVehicle) return Offset.Zero
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
                if (!state.hasMyVehicle) return Offset.Zero
                val delta = available.y
                if (delta > 0f && collapsePx > 0f && listAtTop.value) {
                    val consumedUp = min(delta, collapsePx)
                    collapsePx -= consumedUp
                    return Offset(0f, consumedUp)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!state.hasMyVehicle) return Velocity.Zero
                if (available.y > 0f && collapsePx > 0f && listAtTop.value) {
                    collapsePx = 0f
                    return Velocity(0f, available.y)
                }
                return Velocity.Zero
            }
        }
    }

    var recentExpanded by remember { mutableStateOf(false) }
    var favoritesExpanded by remember { mutableStateOf(false) }
    val showLists = pages.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(nestedScrollConnection),
    ) {
        HomeTopBar(
            searchInTopBar = searchInTopBar,
            merk = state.myVehicleInfo?.merk,
            plate = state.myVehicleKenteken,
            onSearchClick = { onEvent(HomeEvent.OnSearchClick) },
            onFilterClick = { onEvent(HomeEvent.OnFilterClick) },
            onProfileClick = { onEvent(HomeEvent.OnProfileClick) },
        )

        if (heroVisible) {
            val heroHeight = HeroMaxHeight * (1f - collapseProgress)
            HomeHero(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .graphicsLayer {
                        alpha = max(0f, 1f - collapseProgress * 1.2f)
                        scaleX = heroScale
                        scaleY = heroScale
                        translationY = -collapsePx * 0.1f
                    },
            )
        }

        if (!searchInTopBar) {
            HomeSearchRow(
                onSearchClick = { onEvent(HomeEvent.OnSearchClick) },
                onFilterClick = { onEvent(HomeEvent.OnFilterClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
            )
        }

        if (!showLists) {
            Text(
                text = stringResource(Res.string.home_empty_search_prompt),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 24.dp),
            )
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 12.dp,
            ) { pageIndex ->
                val page = pages[pageIndex]
                val listState = when (page) {
                    HomePagerPage.Recent -> recentListState
                    HomePagerPage.Favorites -> favoritesListState
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    item(key = "card-$page") {
                        when (page) {
                            HomePagerPage.Recent -> VehicleListCard(
                                title = stringResource(Res.string.home_section_recent),
                                items = listModels(state.recent, state.recentWinPercent),
                                expanded = recentExpanded,
                                onExpandedChange = { recentExpanded = it },
                                onItemClick = { onEvent(HomeEvent.OnRowClick(it)) },
                                onFavoriteClick = { onEvent(HomeEvent.OnFavoriteClick(it)) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            HomePagerPage.Favorites -> VehicleListCard(
                                title = stringResource(Res.string.home_section_favorites),
                                items = listModels(state.favorites, state.favoriteWinPercent),
                                expanded = favoritesExpanded,
                                onExpandedChange = { favoritesExpanded = it },
                                onItemClick = { onEvent(HomeEvent.OnRowClick(it)) },
                                onFavoriteClick = { onEvent(HomeEvent.OnFavoriteClick(it)) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            if (pages.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    repeat(pages.size) { i ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == i) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                ),
                        )
                    }
                }
            }
        }
    }
}

private enum class HomePagerPage { Recent, Favorites }

@Composable
private fun HomeTopBar(
    searchInTopBar: Boolean,
    merk: String?,
    plate: String?,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BrandMark(merk = merk, size = 40.dp)
        if (searchInTopBar) {
            SearchEntryField(
                onClick = onSearchClick,
                compact = true,
                modifier = Modifier.weight(1f),
            )
            FilterIconButton(onClick = onFilterClick)
        } else {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (plate != null) {
                    LicensePlateBadge(kenteken = plate, compact = true)
                }
            }
        }
        UserAvatar(
            label = stringResource(Res.string.home_profile_placeholder),
            size = 40.dp,
            onClick = onProfileClick,
        )
    }
}

@Composable
private fun HomeSearchRow(
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SearchEntryField(
            onClick = onSearchClick,
            modifier = Modifier.weight(1f),
        )
        FilterIconButton(onClick = onFilterClick)
    }
}

@Composable
private fun HomeHero(
    state: HomeState,
    modifier: Modifier = Modifier,
) {
    val myInfo = state.myVehicleInfo ?: return
    val pk = myInfo.vermogenPk?.toInt()
        ?: myInfo.vermogenKw?.let { (it * 1.36).toInt() }
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
        Text(
            text = "${myInfo.merk} ${myInfo.handelsbenaming}".trim(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(
                Res.string.home_vehicle_subtitle,
                pk?.let { stringResource(Res.string.format_power_pk, it) } ?: "—",
                "—",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PreviewContent() {
    HomeContent(
        state = HomeState(
            recent = listOf(
                previewEntity("PL700K", "VOLVO", "V40", 149.6),
                previewEntity("56ZKD3", "OPEL", "Corsa", 90.0),
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
            recentWinPercent = mapOf("PL700K" to 55f, "56ZKD3" to 25f),
            favoriteKentekens = setOf("56ZKD3"),
            myVehicleAverageWinPercent = 55f,
        ),
        onEvent = {},
        listModels = { entities, winMap ->
            entities.map {
                nl.designlama.pakkiepakkie.ui.components.VehicleListItemModel(
                    kenteken = it.kenteken,
                    title = "${it.merk} ${it.handelsbenaming}",
                    powerPk = it.vermogenPk?.toInt(),
                    winPercent = winMap[it.kenteken],
                    isFavorite = it.kenteken == "56ZKD3",
                )
            }
        },
    )
}

private fun previewEntity(
    kenteken: String,
    merk: String,
    name: String,
    pk: Double,
): VehicleLookupEntity = VehicleLookupEntity(
    kenteken = kenteken,
    merk = merk,
    handelsbenaming = name,
    massaRijklaarKg = 1420,
    vermogenKw = pk / 1.36,
    vermogenPk = pk,
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
)

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    PreviewContainer {
        PreviewContent()
    }
}
