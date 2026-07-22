package nl.designlama.pakkiepakkie.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryVehicleLookupStore : VehicleLookupStore {
    private val mutex = Mutex()
    private val rows = MutableStateFlow<Map<String, VehicleLookupEntity>>(emptyMap())

    override suspend fun upsert(entity: VehicleLookupEntity) {
        mutex.withLock {
            rows.value = rows.value + (entity.kenteken to entity)
        }
    }

    override suspend fun getByKenteken(kenteken: String): VehicleLookupEntity? =
        mutex.withLock { rows.value[kenteken] }

    override suspend fun updateLastViewedAt(kenteken: String, at: Long) {
        mutex.withLock {
            val current = rows.value[kenteken] ?: return
            rows.value = rows.value + (kenteken to current.copy(lastViewedAt = at))
        }
    }

    override suspend fun updateIsChipped(kenteken: String, isChipped: Boolean) {
        mutex.withLock {
            val current = rows.value[kenteken] ?: return
            rows.value = rows.value + (kenteken to current.copy(isChipped = isChipped))
        }
    }

    override fun observeRecent(limit: Int): Flow<List<VehicleLookupEntity>> =
        rows.map { map ->
            map.values.sortedByDescending { it.lastViewedAt }.take(limit)
        }
}
