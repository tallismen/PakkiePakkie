package nl.designlama.pakkiepakkie.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleLookupDao {
    @Upsert
    suspend fun upsert(entity: VehicleLookupRoomEntity)

    @Query("SELECT * FROM vehicle_lookup WHERE kenteken = :kenteken LIMIT 1")
    suspend fun getByKenteken(kenteken: String): VehicleLookupRoomEntity?

    @Query("UPDATE vehicle_lookup SET lastViewedAt = :at WHERE kenteken = :kenteken")
    suspend fun updateLastViewedAt(kenteken: String, at: Long)

    @Query("UPDATE vehicle_lookup SET isChipped = :isChipped WHERE kenteken = :kenteken")
    suspend fun updateIsChipped(kenteken: String, isChipped: Boolean)

    @Query("SELECT * FROM vehicle_lookup ORDER BY lastViewedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<VehicleLookupRoomEntity>>
}
