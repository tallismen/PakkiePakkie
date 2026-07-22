package nl.designlama.pakkiepakkie.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

class WebAppSettings : AppSettings {
    private val mutex = Mutex()
    private val snapshot = MutableStateFlow(readAll())

    override fun observeBoolean(key: String, defaultValue: Boolean): Flow<Boolean> =
        snapshot.map { map ->
            map[key]?.toBooleanStrictOrNull() ?: defaultValue
        }

    override fun observeString(key: String, defaultValue: String?): Flow<String?> =
        snapshot.map { map -> map[key] ?: defaultValue }

    override suspend fun putBoolean(key: String, value: Boolean) {
        putRaw(key, value.toString())
    }

    override suspend fun putString(key: String, value: String) {
        putRaw(key, value)
    }

    private suspend fun putRaw(key: String, value: String) {
        mutex.withLock {
            localStorage["pp_$key"] = value
            snapshot.value = readAll()
        }
    }

    private fun readAll(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for (i in 0 until localStorage.length) {
            val storageKey = localStorage.key(i) ?: continue
            if (!storageKey.startsWith("pp_")) continue
            val value = localStorage[storageKey] ?: continue
            result[storageKey.removePrefix("pp_")] = value
        }
        return result
    }
}
