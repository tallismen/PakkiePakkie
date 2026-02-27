package nl.designlama.pakkiepakkie.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

object PreferencesKeys {
    val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
}

@Single
class PreferencesRepository(private val dataStore: PrefsDataStore) {

    suspend fun saveBoolean(preferenceKey: Preferences.Key<Boolean>, value: Boolean) {
        dataStore.edit { preferences -> preferences[preferenceKey] = value }
    }

    fun getBoolean(preferenceKey: Preferences.Key<Boolean>, defaultValue: Boolean = false): Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[preferenceKey] ?: defaultValue }

    suspend fun saveString(preferenceKey: Preferences.Key<String>, value: String) {
        dataStore.edit { preferences -> preferences[preferenceKey] = value }
    }

    fun getString(preferenceKey: Preferences.Key<String>, defaultValue: String? = null): Flow<String?> =
        dataStore.data.map { preferences -> preferences[preferenceKey] ?: defaultValue }

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun <T> saveObject(preferenceKey: Preferences.Key<String>, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        val jsonString = json.encodeToString(serializer, value)
        dataStore.edit { preferences -> preferences[preferenceKey] = jsonString }
    }

    fun <T> getObject(preferenceKey: Preferences.Key<String>, serializer: kotlinx.serialization.KSerializer<T>, defaultValue: T): Flow<T> =
        dataStore.data.map { preferences ->
            preferences[preferenceKey]?.let { jsonString ->
                try {
                    json.decodeFromString(serializer, jsonString)
                } catch (e: Exception) {
                    defaultValue
                }
            } ?: defaultValue
        }
}
