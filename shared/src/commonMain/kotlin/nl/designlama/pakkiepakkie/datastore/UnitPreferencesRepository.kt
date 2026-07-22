package nl.designlama.pakkiepakkie.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.designlama.pakkiepakkie.domain.units.PowerUnit
import nl.designlama.pakkiepakkie.domain.units.UnitConversions
import nl.designlama.pakkiepakkie.domain.units.UnitPreferences
import nl.designlama.pakkiepakkie.domain.units.UnitPreset
import nl.designlama.pakkiepakkie.domain.units.WeightUnit
import org.koin.core.annotation.Single

@Single
class UnitPreferencesRepository(
    private val preferencesRepository: PreferencesRepository,
) {
    val preferencesFlow: Flow<UnitPreferences> = combine(
        preferencesRepository.getString(PreferencesKeys.UNIT_PRESET),
        preferencesRepository.getString(PreferencesKeys.POWER_UNIT),
        preferencesRepository.getString(PreferencesKeys.WEIGHT_UNIT),
    ) { presetRaw, powerRaw, weightRaw ->
        UnitPreferences(
            preset = presetRaw?.let { runCatching { UnitPreset.valueOf(it) }.getOrNull() }
                ?: UnitPreset.Standaard,
            powerUnit = powerRaw?.let { runCatching { PowerUnit.valueOf(it) }.getOrNull() }
                ?: PowerUnit.Kw,
            weightUnit = weightRaw?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() }
                ?: WeightUnit.Kg,
        )
    }

    suspend fun setPreset(preset: UnitPreset) {
        val (power, weight) = UnitConversions.presetUnits(preset)
        preferencesRepository.saveString(PreferencesKeys.UNIT_PRESET, preset.name)
        preferencesRepository.saveString(PreferencesKeys.POWER_UNIT, power.name)
        preferencesRepository.saveString(PreferencesKeys.WEIGHT_UNIT, weight.name)
    }

    suspend fun setPowerUnit(unit: PowerUnit) {
        preferencesRepository.saveString(PreferencesKeys.POWER_UNIT, unit.name)
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        preferencesRepository.saveString(PreferencesKeys.WEIGHT_UNIT, unit.name)
    }
}
