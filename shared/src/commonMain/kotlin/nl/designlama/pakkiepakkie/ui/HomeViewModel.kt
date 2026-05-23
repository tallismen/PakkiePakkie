package nl.designlama.pakkiepakkie.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import nl.designlama.pakkiepakkie.base.BaseViewModel
import nl.designlama.pakkiepakkie.base.UIEvent
import nl.designlama.pakkiepakkie.base.UIState
import nl.designlama.pakkiepakkie.data.VehicleLicenseRepository
import nl.designlama.pakkiepakkie.data.local.VehicleLookupDataVersion
import nl.designlama.pakkiepakkie.data.local.VehicleLookupEntity
import nl.designlama.pakkiepakkie.data.toVehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.datastore.UserVehicleRepository
import nl.designlama.pakkiepakkie.network.rdw.PakkiePakkieCalculator
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.MAX_RAW_LENGTH
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate

data class HomeState(
    val licensePlateInput: String = "",
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val recent: List<VehicleLookupEntity> = emptyList(),
    val myVehicleKenteken: String? = null,
    val myVehicleInfo: VehicleLicensePlateInfo? = null,
    val recentWinPercent: Map<String, Float?> = emptyMap(),
) : UIState {
    val canSearch: Boolean get() = licensePlateInput.length == MAX_RAW_LENGTH && !loading
}

sealed interface HomeEvent : UIEvent {
    data class OnLicensePlateChange(val raw: String) : HomeEvent
    data object OnSearchClick : HomeEvent
    data class OnRecentRowClick(val kenteken: String) : HomeEvent
    data class OnRecentSetMyVehicle(val kenteken: String) : HomeEvent
    data object OnSettingsClick : HomeEvent
}

class HomeViewModel(
    private val vehicleLicenseRepository: VehicleLicenseRepository,
    private val userVehicleRepository: UserVehicleRepository,
) : BaseViewModel<HomeState, HomeEvent, HomeDirections>() {

    private val lookupMutex = Mutex()

    init {
        viewModelScope.launch {
            combine(
                vehicleLicenseRepository.observeRecent(20),
                userVehicleRepository.myVehicleKentekenFlow(),
            ) { recent, myK ->
                Pair(recent, myK)
            }.collectLatest { (recent, myK) ->
                val myInfo = withContext(Dispatchers.Default) {
                    resolveMyVehicleInfo(recent, myK)
                }
                val winMap = withContext(Dispatchers.Default) {
                    buildRecentWinPercentMap(myInfo, recent)
                }
                _state.value = _state.value.copy(
                    recent = recent,
                    myVehicleKenteken = myK,
                    myVehicleInfo = myInfo,
                    recentWinPercent = winMap,
                )
            }
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

    private fun buildRecentWinPercentMap(
        myInfo: VehicleLicensePlateInfo?,
        recent: List<VehicleLookupEntity>,
    ): Map<String, Float?> {
        if (myInfo == null) return recent.associate { it.kenteken to null }
        return recent.associate { row ->
            val other = row.toVehicleLicensePlateInfo()
            row.kenteken to PakkiePakkieCalculator.winProbabilityPercent(myInfo, other)
        }
    }

    override fun defaultUIState(): HomeState = HomeState()

    override fun onEvent(event: HomeEvent) {
        super.onEvent(event)
        when (event) {
            is HomeEvent.OnLicensePlateChange -> {
                _state.value = _state.value.copy(licensePlateInput = event.raw, errorMessage = null)
            }
            HomeEvent.OnSearchClick -> performLookup(_state.value.licensePlateInput)
            is HomeEvent.OnRecentRowClick -> {
                _state.value = _state.value.copy(licensePlateInput = event.kenteken, errorMessage = null)
                navigate(HomeDirections.OpenVehicleDetail(event.kenteken))
            }
            is HomeEvent.OnRecentSetMyVehicle -> {
                viewModelScope.launch {
                    val norm = sanitizeLicensePlate(event.kenteken)
                    if (norm.length != 6) return@launch
                    val current = _state.value.myVehicleKenteken?.let { sanitizeLicensePlate(it) }
                    if (current == norm) {
                        runCatching { userVehicleRepository.clearMyVehicle() }
                    } else {
                        runCatching { userVehicleRepository.setMyVehicle(event.kenteken) }
                    }
                }
            }
            HomeEvent.OnSettingsClick -> navigate(HomeDirections.OpenSettings)
        }
    }

    private fun performLookup(raw: String) {
        viewModelScope.launch {
            if (!lookupMutex.tryLock()) return@launch
            try {
                val norm = sanitizeLicensePlate(raw)
                if (norm.length != 6) {
                    _state.value = _state.value.copy(
                        loading = false,
                        errorMessage = "Kenteken moet 6 tekens zijn",
                    )
                    return@launch
                }
                _state.value = _state.value.copy(loading = true, errorMessage = null)
                val recentRow = _state.value.recent.find { sanitizeLicensePlate(it.kenteken) == norm }
                val fromRecent = recentRow?.takeIf { it.dataVersion >= VehicleLookupDataVersion.FULL }
                    ?.toVehicleLicensePlateInfo()
                    ?.takeIf { it.hasSufficientCachedFields() }
                if (fromRecent != null) {
                    vehicleLicenseRepository.markRecentlyViewed(norm)
                    _state.value = _state.value.copy(loading = false)
                    navigate(HomeDirections.OpenVehicleDetail(norm))
                    return@launch
                }
                val result = vehicleLicenseRepository.loadCachedOrRefresh(raw)
                result.onSuccess {
                    _state.value = _state.value.copy(loading = false)
                    navigate(HomeDirections.OpenVehicleDetail(norm))
                }.onFailure { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        errorMessage = error.message ?: error.toString(),
                    )
                }
            } finally {
                lookupMutex.unlock()
            }
        }
    }
}
