package nl.designlama.pakkiepakkie.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import nl.designlama.pakkiepakkie.base.BaseViewModel
import nl.designlama.pakkiepakkie.base.UIEvent
import nl.designlama.pakkiepakkie.base.UIState
import nl.designlama.pakkiepakkie.data.VehicleLicenseRepository
import nl.designlama.pakkiepakkie.data.local.VehicleLookupDataVersion
import nl.designlama.pakkiepakkie.data.toVehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.resources.StringResources
import nl.designlama.pakkiepakkie.ui.components.MAX_RAW_LENGTH
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate

data class SearchState(
    val licensePlateInput: String = "",
    val loading: Boolean = false,
    val errorMessage: String? = null,
) : UIState {
    val canSearch: Boolean get() = licensePlateInput.length == MAX_RAW_LENGTH && !loading
}

sealed interface SearchEvent : UIEvent {
    data class OnLicensePlateChange(val raw: String) : SearchEvent
    data object OnSearchClick : SearchEvent
    data object OnProfileClick : SearchEvent
}

class SearchViewModel(
    private val vehicleLicenseRepository: VehicleLicenseRepository,
) : BaseViewModel<SearchState, SearchEvent, SearchDirections>() {

    private val lookupMutex = Mutex()

    override fun defaultUIState(): SearchState = SearchState()

    override fun onEvent(event: SearchEvent) {
        super.onEvent(event)
        when (event) {
            is SearchEvent.OnLicensePlateChange -> {
                _state.value = _state.value.copy(licensePlateInput = event.raw, errorMessage = null)
                if (event.raw.length == MAX_RAW_LENGTH) {
                    performLookup(event.raw)
                }
            }
            SearchEvent.OnSearchClick -> performLookup(_state.value.licensePlateInput)
            SearchEvent.OnProfileClick -> navigate(SearchDirections.OpenProfile)
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
                        errorMessage = StringResources.kentekenMustBeSixChars(),
                    )
                    return@launch
                }
                _state.value = _state.value.copy(loading = true, errorMessage = null)
                val cached = vehicleLicenseRepository.getCachedEntity(norm)
                    ?.takeIf { it.dataVersion >= VehicleLookupDataVersion.FULL }
                    ?.toVehicleLicensePlateInfo()
                    ?.takeIf { it.hasSufficientCachedFields() }
                if (cached != null) {
                    vehicleLicenseRepository.markRecentlyViewed(norm)
                    _state.value = _state.value.copy(loading = false)
                    navigate(SearchDirections.OpenVehicleDetail(norm))
                    return@launch
                }
                val result = vehicleLicenseRepository.loadCachedOrRefresh(raw)
                result.onSuccess {
                    _state.value = _state.value.copy(loading = false)
                    navigate(SearchDirections.OpenVehicleDetail(norm))
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
