package nl.designlama.pakkiepakkie.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
    )

const val dataStoreFileName = "pakkiepakkie.preferences_pb"

typealias PrefsDataStore = DataStore<Preferences>

class DataStoreAppSettings(
    private val dataStore: PrefsDataStore,
) : AppSettings {
    override fun observeBoolean(key: String, defaultValue: Boolean): Flow<Boolean> {
        val prefKey = booleanPreferencesKey(key)
        return dataStore.data.map { it[prefKey] ?: defaultValue }
    }

    override fun observeString(key: String, defaultValue: String?): Flow<String?> {
        val prefKey = stringPreferencesKey(key)
        return dataStore.data.map { it[prefKey] ?: defaultValue }
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        val prefKey = booleanPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }

    override suspend fun putString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { it[prefKey] = value }
    }
}
