package nl.designlama.pakkiepakkie.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleLookupDao {
    @Upsert
    suspend fun upsert(entity: VehicleLookupEntity)

    @Query("SELECT * FROM vehicle_lookup WHERE kenteken = :kenteken LIMIT 1")
    suspend fun getByKenteken(kenteken: String): VehicleLookupEntity?

    @Query("UPDATE vehicle_lookup SET lastViewedAt = :at WHERE kenteken = :kenteken")
    suspend fun updateLastViewedAt(kenteken: String, at: Long)

    @Query("SELECT * FROM vehicle_lookup ORDER BY lastViewedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<VehicleLookupEntity>>
}
