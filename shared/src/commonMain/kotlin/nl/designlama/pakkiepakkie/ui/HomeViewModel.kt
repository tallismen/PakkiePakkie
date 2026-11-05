package nl.designlama.pakkiepakkie.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.designlama.pakkiepakkie.base.BaseViewModel
import nl.designlama.pakkiepakkie.base.UIEvent
import nl.designlama.pakkiepakkie.base.UIState
import nl.designlama.pakkiepakkie.data.VehicleLicenseRepository
import nl.designlama.pakkiepakkie.data.local.VehicleLookupDataVersion
import nl.designlama.pakkiepakkie.data.local.VehicleLookupEntity
import nl.designlama.pakkiepakkie.data.toVehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.datastore.FavoritesRepository
import nl.designlama.pakkiepakkie.datastore.UserVehicleRepository
import nl.designlama.pakkiepakkie.network.chipped.ChippedTuneCalculator
import nl.designlama.pakkiepakkie.network.rdw.PakkiePakkieCalculator
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.VehicleListItemModel
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate

data class HomeState(
    val recent: List<VehicleLookupEntity> = emptyList(),
    val favorites: List<VehicleLookupEntity> = emptyList(),
    val favoriteKentekens: Set<String> = emptySet(),
    val myVehicleKenteken: String? = null,
    val myVehicleInfo: VehicleLicensePlateInfo? = null,
    val recentWinPercent: Map<String, Float?> = emptyMap(),
    val favoriteWinPercent: Map<String, Float?> = emptyMap(),
    /** Average win chance of my vehicle vs other recently viewed plates. */
    val myVehicleAverageWinPercent: Float? = null,
    val snackbarMessage: String? = null,
) : UIState {
    val hasMyVehicle: Boolean get() = myVehicleInfo != null
}

sealed interface HomeEvent : UIEvent {
    data class OnRowClick(val kenteken: String) : HomeEvent
    data class OnFavoriteClick(val kenteken: String) : HomeEvent
    data object OnSearchClick : HomeEvent
    data object OnFilterClick : HomeEvent
    data object OnProfileClick : HomeEvent
    data object OnSnackbarDismissed : HomeEvent
}

class HomeViewModel(
    private val vehicleLicenseRepository: VehicleLicenseRepository,
    private val userVehicleRepository: UserVehicleRepository,
    private val favoritesRepository: FavoritesRepository,
) : BaseViewModel<HomeState, HomeEvent, HomeDirections>() {

    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbar: SharedFlow<String> = _snackbar.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                vehicleLicenseRepository.observeRecent(20),
                userVehicleRepository.myVehicleKentekenFlow(),
                favoritesRepository.favoritesFlow(),
            ) { recent, myK, favoriteKeys ->
                Triple(recent, myK, favoriteKeys)
            }.collectLatest { (recent, myK, favoriteKeys) ->
                val myInfo = withContext(Dispatchers.Default) {
                    resolveMyVehicleInfo(recent, myK)
                }
                val myEntity = myK?.let { k ->
                    recent.find { sanitizeLicensePlate(it.kenteken) == sanitizeLicensePlate(k) }
                        ?: vehicleLicenseRepository.getCachedEntity(k)
                }
                val favorites = withContext(Dispatchers.Default) {
                    resolveFavoriteEntities(favoriteKeys, recent)
                }
                val winMap = withContext(Dispatchers.Default) {
                    buildWinPercentMap(myInfo, myEntity, recent)
                }
                val favWinMap = withContext(Dispatchers.Default) {
                    buildWinPercentMap(myInfo, myEntity, favorites)
                }
                val averageWin = withContext(Dispatchers.Default) {
                    averageWinPercentExcludingSelf(myK, winMap)
                }
                _state.value = _state.value.copy(
                    recent = recent,
                    favorites = favorites,
                    favoriteKentekens = favoriteKeys.map { sanitizeLicensePlate(it) }.toSet(),
                    myVehicleKenteken = myK,
                    myVehicleInfo = myInfo,
                    recentWinPercent = winMap,
                    favoriteWinPercent = favWinMap,
                    myVehicleAverageWinPercent = averageWin,
                )
            }
        }
    }

    fun toListModels(
        entities: List<VehicleLookupEntity>,
        winMap: Map<String, Float?>,
        favorites: Set<String>,
    ): List<VehicleListItemModel> = entities.map { row ->
        VehicleListItemModel(
            kenteken = row.kenteken,
            title = "${row.merk} ${row.handelsbenaming}".trim(),
            powerPk = row.vermogenPk?.toInt()
                ?: row.vermogenKw?.let { (it * 1.36).toInt() },
            winPercent = winMap[row.kenteken],
            isFavorite = favorites.contains(sanitizeLicensePlate(row.kenteken)),
        )
    }

    private suspend fun resolveFavoriteEntities(
        keys: List<String>,
        recent: List<VehicleLookupEntity>,
    ): List<VehicleLookupEntity> {
        return keys.mapNotNull { key ->
            val norm = sanitizeLicensePlate(key)
            recent.find { sanitizeLicensePlate(it.kenteken) == norm }
                ?: vehicleLicenseRepository.getCachedEntity(norm)
        }
    }

    private suspend fun resolveMyVehicleInfo(
        recent: List<VehicleLookupEntity>,
        myK: String?,
    ): VehicleLicensePlateInfo? {
        val norm = myK?.let { sanitizeLicensePlate(it) }?.takeIf { it.length == 6 } ?: return null
        recent.find { sanitizeLicensePlate(it.kenteken) == norm }
            ?.takeIf { it.dataVersion >= VehicleLookupDataVersion.FULL }
            ?.toVehicleLicensePlateInfo()
            ?.let { return it }
        return vehicleLicenseRepository.getCachedEntity(norm)
            ?.takeIf { it.dataVersion >= VehicleLookupDataVersion.FULL }
            ?.toVehicleLicensePlateInfo()
    }

    private fun buildWinPercentMap(
        myInfo: VehicleLicensePlateInfo?,
        myEntity: VehicleLookupEntity?,
        rows: List<VehicleLookupEntity>,
    ): Map<String, Float?> {
        if (myInfo == null) return rows.associate { it.kenteken to null }
        val myKw = effectiveKw(myInfo, myEntity?.isChipped == true)
        return rows.associate { row ->
            val other = row.toVehicleLicensePlateInfo()
            val otherKw = effectiveKw(other, row.isChipped)
            row.kenteken to PakkiePakkieCalculator.winProbabilityPercent(
                my = myInfo,
                other = other,
                myVermogenKwOverride = myKw,
                otherVermogenKwOverride = otherKw,
            )
        }
    }

    private fun averageWinPercentExcludingSelf(
        myKenteken: String?,
        winMap: Map<String, Float?>,
    ): Float? {
        val myNorm = myKenteken?.let { sanitizeLicensePlate(it) } ?: return null
        val values = winMap.mapNotNull { (kenteken, percent) ->
            if (sanitizeLicensePlate(kenteken) == myNorm) null else percent
        }
        if (values.isEmpty()) return null
        return values.average().toFloat()
    }

    private fun effectiveKw(info: VehicleLicensePlateInfo, isChipped: Boolean): Double? {
        if (!isChipped) return null
        return ChippedTuneCalculator.estimate(info)?.stage1Kw
    }

    override fun defaultUIState(): HomeState = HomeState()

    override fun onEvent(event: HomeEvent) {
        super.onEvent(event)
        when (event) {
            is HomeEvent.OnRowClick -> {
                navigate(HomeDirections.OpenVehicleDetail(event.kenteken))
            }
            is HomeEvent.OnFavoriteClick -> {
                viewModelScope.launch {
                    val added = favoritesRepository.toggleFavorite(event.kenteken)
                    _snackbar.emit(
                        if (added) SNACKBAR_ADDED else SNACKBAR_REMOVED,
                    )
                }
            }
            HomeEvent.OnSearchClick, HomeEvent.OnFilterClick -> {
                navigate(HomeDirections.OpenSearch)
            }
            HomeEvent.OnProfileClick -> navigate(HomeDirections.OpenProfile)
            HomeEvent.OnSnackbarDismissed -> {
                _state.value = _state.value.copy(snackbarMessage = null)
            }
        }
    }

    companion object {
        const val SNACKBAR_ADDED = "favorite_added"
        const val SNACKBAR_REMOVED = "favorite_removed"
    }
}
