package nl.designlama.pakkiepakkie.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.designlama.pakkiepakkie.ui.components.sanitizeLicensePlate
import org.koin.core.annotation.Single

@Single
class UserVehicleRepository(
    private val preferencesRepository: PreferencesRepository,
) {
    fun myVehicleKentekenFlow(): Flow<String?> =
        preferencesRepository.getString(PreferencesKeys.MY_VEHICLE_KENTEKEN, null).map { raw ->
            raw?.trim()?.takeIf { it.isNotEmpty() }?.let { sanitizeLicensePlate(it).takeIf { k -> k.length == 6 } }
        }

    suspend fun setMyVehicle(rawKenteken: String) {
        val norm = sanitizeLicensePlate(rawKenteken)
        require(norm.length == 6) { "Kenteken moet 6 tekens zijn" }
        preferencesRepository.saveString(PreferencesKeys.MY_VEHICLE_KENTEKEN, norm)
    }

    suspend fun clearMyVehicle() {
        preferencesRepository.saveString(PreferencesKeys.MY_VEHICLE_KENTEKEN, "")
    }
}
