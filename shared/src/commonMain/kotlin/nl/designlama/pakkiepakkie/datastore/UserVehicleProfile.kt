package nl.designlama.pakkiepakkie.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

enum class TireType {
    Summer,
    Winter,
    AllSeason,
}

@Serializable
data class UserVehicleProfile(
    val mileageKm: Int? = null,
    val tires: TireType = TireType.Summer,
    val hasNitrous: Boolean = false,
    val hasModdedExhaust: Boolean = false,
)

@Single
class UserVehicleProfileRepository(
    private val preferencesRepository: PreferencesRepository,
) {
    fun profileFlow(): Flow<UserVehicleProfile> = combine(
        preferencesRepository.getString(PreferencesKeys.VEHICLE_MILEAGE_KM, null),
        preferencesRepository.getString(PreferencesKeys.VEHICLE_TIRES, TireType.Summer.name),
        preferencesRepository.getBoolean(PreferencesKeys.VEHICLE_HAS_NITROUS, false),
        preferencesRepository.getBoolean(PreferencesKeys.VEHICLE_HAS_MODDED_EXHAUST, false),
    ) { mileageRaw, tiresRaw, nitrous, exhaust ->
        UserVehicleProfile(
            mileageKm = mileageRaw?.toIntOrNull(),
            tires = tiresRaw?.let { runCatching { TireType.valueOf(it) }.getOrNull() } ?: TireType.Summer,
            hasNitrous = nitrous,
            hasModdedExhaust = exhaust,
        )
    }

    suspend fun setMileageKm(value: Int?) {
        preferencesRepository.saveString(
            PreferencesKeys.VEHICLE_MILEAGE_KM,
            value?.toString().orEmpty(),
        )
    }

    suspend fun setTires(tires: TireType) {
        preferencesRepository.saveString(PreferencesKeys.VEHICLE_TIRES, tires.name)
    }

    suspend fun setHasNitrous(value: Boolean) {
        preferencesRepository.saveBoolean(PreferencesKeys.VEHICLE_HAS_NITROUS, value)
    }

    suspend fun setHasModdedExhaust(value: Boolean) {
        preferencesRepository.saveBoolean(PreferencesKeys.VEHICLE_HAS_MODDED_EXHAUST, value)
    }

    suspend fun clearProfile() {
        setMileageKm(null)
        setTires(TireType.Summer)
        setHasNitrous(false)
        setHasModdedExhaust(false)
    }
}
