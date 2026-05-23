package nl.designlama.pakkiepakkie.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import nl.designlama.pakkiepakkie.base.BaseViewModel
import nl.designlama.pakkiepakkie.base.UIEvent
import nl.designlama.pakkiepakkie.base.UIState
import nl.designlama.pakkiepakkie.datastore.UnitPreferencesRepository
import nl.designlama.pakkiepakkie.domain.units.PowerUnit
import nl.designlama.pakkiepakkie.domain.units.UnitPreferences
import nl.designlama.pakkiepakkie.domain.units.UnitPreset
import nl.designlama.pakkiepakkie.domain.units.VehicleDisplayFormatter
import nl.designlama.pakkiepakkie.domain.units.WeightUnit

data class SettingsState(
    val preferences: UnitPreferences = UnitPreferences(),
    val previewPower: String = "",
    val previewWeight: String = "",
) : UIState

sealed interface SettingsEvent : UIEvent {
    data class OnPresetSelected(val preset: UnitPreset) : SettingsEvent
    data class OnPowerUnitSelected(val unit: PowerUnit) : SettingsEvent
    data class OnWeightUnitSelected(val unit: WeightUnit) : SettingsEvent
}

class SettingsViewModel(
    private val unitPreferencesRepository: UnitPreferencesRepository,
    private val displayFormatter: VehicleDisplayFormatter,
) : BaseViewModel<SettingsState, SettingsEvent, SettingsDirections>() {

    private companion object {
        const val PREVIEW_KW = 110.0
        const val PREVIEW_KG = 1420
    }

    init {
        viewModelScope.launch {
            unitPreferencesRepository.preferencesFlow.collectLatest { prefs ->
                _state.value = _state.value.copy(
                    preferences = prefs,
                    previewPower = displayFormatter.formatPower(PREVIEW_KW, prefs),
                    previewWeight = displayFormatter.formatWeight(PREVIEW_KG, prefs),
                )
            }
        }
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
        }
    }
}
