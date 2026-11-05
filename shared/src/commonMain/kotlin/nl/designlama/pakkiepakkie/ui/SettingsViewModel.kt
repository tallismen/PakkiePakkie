package nl.designlama.pakkiepakkie.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.designlama.pakkiepakkie.base.BaseViewModel
import nl.designlama.pakkiepakkie.base.UIEvent
import nl.designlama.pakkiepakkie.base.UIState
import nl.designlama.pakkiepakkie.data.VehicleLicenseRepository
import nl.designlama.pakkiepakkie.data.local.VehicleLookupDataVersion
import nl.designlama.pakkiepakkie.data.toVehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.datastore.TireType
import nl.designlama.pakkiepakkie.datastore.UnitPreferencesRepository
import nl.designlama.pakkiepakkie.datastore.UserVehicleProfile
import nl.designlama.pakkiepakkie.datastore.UserVehicleProfileRepository
import nl.designlama.pakkiepakkie.datastore.UserVehicleRepository
import nl.designlama.pakkiepakkie.domain.units.PowerUnit
import nl.designlama.pakkiepakkie.domain.units.UnitPreferences
import nl.designlama.pakkiepakkie.domain.units.UnitPreset
import nl.designlama.pakkiepakkie.domain.units.VehicleDisplayFormatter
import nl.designlama.pakkiepakkie.domain.units.WeightUnit
import nl.designlama.pakkiepakkie.network.rdw.VehicleLicensePlateInfo
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate
import nl.designlama.pakkiepakkie.utils.AppConfig

data class SettingsState(
    val preferences: UnitPreferences = UnitPreferences(),
    val previewPower: String = "",
    val previewWeight: String = "",
    val versionName: String = "",
    val versionCode: String = "",
    val myVehicleKenteken: String? = null,
    val myVehicleInfo: VehicleLicensePlateInfo? = null,
    val myIsChipped: Boolean = false,
    val profile: UserVehicleProfile = UserVehicleProfile(),
    val mileageInput: String = "",
) : UIState

sealed interface SettingsEvent : UIEvent {
    data class OnPresetSelected(val preset: UnitPreset) : SettingsEvent
    data class OnPowerUnitSelected(val unit: PowerUnit) : SettingsEvent
    data class OnWeightUnitSelected(val unit: WeightUnit) : SettingsEvent
    data object OnVersionTapped : SettingsEvent
    data class OnMileageChange(val raw: String) : SettingsEvent
    data object OnMileageCommit : SettingsEvent
    data class OnTiresSelected(val tires: TireType) : SettingsEvent
    data object OnToggleChipped : SettingsEvent
    data object OnToggleNitrous : SettingsEvent
    data object OnToggleModdedExhaust : SettingsEvent
    data object OnDeleteVehicle : SettingsEvent
}

class SettingsViewModel(
    private val unitPreferencesRepository: UnitPreferencesRepository,
    private val displayFormatter: VehicleDisplayFormatter,
    private val appConfig: AppConfig,
    private val userVehicleRepository: UserVehicleRepository,
    private val vehicleLicenseRepository: VehicleLicenseRepository,
    private val profileRepository: UserVehicleProfileRepository,
) : BaseViewModel<SettingsState, SettingsEvent, SettingsDirections>() {

    private companion object {
        const val PREVIEW_KW = 110.0
        const val PREVIEW_KG = 1420
        const val TAPS_TO_OPEN_TEST_MENU = 10
        const val TAP_RESET_TIMEOUT_MS = 2_000L
    }

    private var versionTapCount = 0
    private var tapResetJob: Job? = null

    init {
        _state.value = _state.value.copy(
            versionName = appConfig.versionName,
            versionCode = appConfig.versionCode,
        )
        viewModelScope.launch {
            unitPreferencesRepository.preferencesFlow.collectLatest { prefs ->
                _state.value = _state.value.copy(
                    preferences = prefs,
                    previewPower = displayFormatter.formatPower(PREVIEW_KW, prefs),
                    previewWeight = displayFormatter.formatWeight(PREVIEW_KG, prefs),
                )
            }
        }
        viewModelScope.launch {
            combine(
                userVehicleRepository.myVehicleKentekenFlow(),
                profileRepository.profileFlow(),
            ) { myK, profile ->
                myK to profile
            }.collectLatest { (myK, profile) ->
                val info = myK?.let { resolveInfo(it) }
                val chipped = myK?.let { vehicleLicenseRepository.isChipped(it) } ?: false
                _state.value = _state.value.copy(
                    myVehicleKenteken = myK,
                    myVehicleInfo = info,
                    myIsChipped = chipped,
                    profile = profile,
                    mileageInput = profile.mileageKm?.toString().orEmpty(),
                )
            }
        }
    }

    private suspend fun resolveInfo(myK: String): VehicleLicensePlateInfo? {
        val norm = sanitizeLicensePlate(myK).takeIf { it.length == 6 } ?: return null
        return vehicleLicenseRepository.getCachedEntity(norm)
            ?.takeIf { it.dataVersion >= VehicleLookupDataVersion.FULL }
            ?.toVehicleLicensePlateInfo()
    }

    override fun defaultUIState(): SettingsState = SettingsState()

    override fun onEvent(event: SettingsEvent) {
        super.onEvent(event)
        when (event) {
            is SettingsEvent.OnPresetSelected -> {
                viewModelScope.launch {
                    unitPreferencesRepository.setPreset(event.preset)
                }
            }
            is SettingsEvent.OnPowerUnitSelected -> {
                viewModelScope.launch {
                    unitPreferencesRepository.setPowerUnit(event.unit)
                }
            }
            is SettingsEvent.OnWeightUnitSelected -> {
                viewModelScope.launch {
                    unitPreferencesRepository.setWeightUnit(event.unit)
                }
            }
            SettingsEvent.OnVersionTapped -> {
                versionTapCount += 1
                tapResetJob?.cancel()
                if (versionTapCount >= TAPS_TO_OPEN_TEST_MENU) {
                    versionTapCount = 0
                    navigate(SettingsDirections.OpenTestMenu)
                } else {
                    tapResetJob = viewModelScope.launch {
                        delay(TAP_RESET_TIMEOUT_MS)
                        versionTapCount = 0
                    }
                }
            }
            is SettingsEvent.OnMileageChange -> {
                _state.value = _state.value.copy(
                    mileageInput = event.raw.filter { it.isDigit() || it == '.' }.take(10),
                )
            }
            SettingsEvent.OnMileageCommit -> {
                viewModelScope.launch {
                    val km = _state.value.mileageInput
                        .replace(".", "")
                        .toIntOrNull()
                    profileRepository.setMileageKm(km)
                }
            }
            is SettingsEvent.OnTiresSelected -> {
                viewModelScope.launch {
                    profileRepository.setTires(event.tires)
                }
            }
            SettingsEvent.OnToggleChipped -> {
                val kenteken = _state.value.myVehicleKenteken ?: return
                viewModelScope.launch {
                    val next = !_state.value.myIsChipped
                    vehicleLicenseRepository.setChipped(kenteken, next)
                    _state.value = _state.value.copy(myIsChipped = next)
                }
            }
            SettingsEvent.OnToggleNitrous -> {
                viewModelScope.launch {
                    profileRepository.setHasNitrous(!_state.value.profile.hasNitrous)
                }
            }
            SettingsEvent.OnToggleModdedExhaust -> {
                viewModelScope.launch {
                    profileRepository.setHasModdedExhaust(!_state.value.profile.hasModdedExhaust)
                }
            }
            SettingsEvent.OnDeleteVehicle -> {
                viewModelScope.launch {
                    userVehicleRepository.clearMyVehicle()
                    profileRepository.clearProfile()
                }
            }
        }
    }
}
