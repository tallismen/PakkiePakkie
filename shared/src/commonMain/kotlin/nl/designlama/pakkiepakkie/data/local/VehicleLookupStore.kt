package nl.designlama.pakkiepakkie.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Platform vehicle cache. Room on Android/iOS; in-memory on web.
 */
interface VehicleLookupStore {
    suspend fun upsert(entity: VehicleLookupEntity)
    suspend fun getByKenteken(kenteken: String): VehicleLookupEntity?
    suspend fun updateLastViewedAt(kenteken: String, at: Long)
    suspend fun updateIsChipped(kenteken: String, isChipped: Boolean)
    fun observeRecent(limit: Int): Flow<List<VehicleLookupEntity>>
}
