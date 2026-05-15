package nl.designlama.pakkiepakkie.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.designlama.pakkiepakkie.base.BaseViewModel
import nl.designlama.pakkiepakkie.base.UIDirections
import nl.designlama.pakkiepakkie.base.UIEvent
import nl.designlama.pakkiepakkie.base.UIState
import nl.designlama.pakkiepakkie.data.VehicleLicenseRepository
import nl.designlama.pakkiepakkie.data.local.VehicleLookupDataVersion
import nl.designlama.pakkiepakkie.data.toVehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.datastore.UserVehicleRepository
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate

data class VehicleDetailState(
    val loading: Boolean = true,
    val detail: VehicleLicensePlateInfo? = null,
    val my: VehicleLicensePlateInfo? = null,
    val errorMessage: String? = null,
) : UIState

sealed interface VehicleDetailEvent : UIEvent

class VehicleDetailViewModel(
    private val vehicleLicenseRepository: VehicleLicenseRepository,
    private val userVehicleRepository: UserVehicleRepository,
    private val kenteken: String,
) : BaseViewModel<VehicleDetailState, VehicleDetailEvent, UIDirections>() {

    init {
        viewModelScope.launch {
            val norm = sanitizeLicensePlate(kenteken)
            if (norm.length != 6) {
                _state.value = VehicleDetailState(loading = false, errorMessage = "Ongeldig kenteken")
                return@launch
            }
            val entity = vehicleLicenseRepository.getCachedEntity(norm)
            val result = if (entity != null && entity.dataVersion >= VehicleLookupDataVersion.FULL) {
                Result.success(entity.toVehicleLicensePlateInfo())
            } else {
                vehicleLicenseRepository.refreshByKenteken(norm)
            }
            val detail = result.getOrNull()
            if (detail == null) {
                _state.value = VehicleDetailState(
                    loading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Kon voertuig niet laden",
                )
                return@launch
            }
            val myK = userVehicleRepository.myVehicleKentekenFlow().first()
            val myNorm = myK?.let { sanitizeLicensePlate(it) }?.takeIf { it.length == 6 }
            val myEntity = myNorm?.let { vehicleLicenseRepository.getCachedEntity(it) }
            val myInfo = myEntity
                ?.takeIf { it.dataVersion >= VehicleLookupDataVersion.FULL }
                ?.toVehicleLicensePlateInfo()
            _state.value = VehicleDetailState(loading = false, detail = detail, my = myInfo, errorMessage = null)
        }
    }

    override fun defaultUIState(): VehicleDetailState = VehicleDetailState()
}
