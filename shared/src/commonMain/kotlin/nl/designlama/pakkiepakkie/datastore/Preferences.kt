package nl.designlama.pakkiepakkie.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

object PreferencesKeys {
    const val ONBOARDING_SEEN = "onboarding_seen"
    const val MY_VEHICLE_KENTEKEN = "my_vehicle_kenteken"
    const val UNIT_PRESET = "unit_preset"
    const val POWER_UNIT = "power_unit"
    const val WEIGHT_UNIT = "weight_unit"
}

interface AppSettings {
    fun observeBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean>
    fun observeString(key: String, defaultValue: String? = null): Flow<String?>
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun putString(key: String, value: String)
}

@Single
class PreferencesRepository(private val settings: AppSettings) {

    suspend fun saveBoolean(key: String, value: Boolean) {
        settings.putBoolean(key, value)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean> =
        settings.observeBoolean(key, defaultValue)

    suspend fun saveString(key: String, value: String) {
        settings.putString(key, value)
    }

    fun getString(key: String, defaultValue: String? = null): Flow<String?> =
        settings.observeString(key, defaultValue)

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun <T> saveObject(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        settings.putString(key, json.encodeToString(serializer, value))
    }

    fun <T> getObject(
        key: String,
        serializer: kotlinx.serialization.KSerializer<T>,
        defaultValue: T,
    ): Flow<T> =
        settings.observeString(key).map { raw ->
            raw?.let { jsonString ->
                try {
                    json.decodeFromString(serializer, jsonString)
                } catch (_: Exception) {
                    defaultValue
                }
            } ?: defaultValue
        }
}
