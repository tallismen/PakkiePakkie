package nl.designlama.pakkiepakkie.data.local

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Builder
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Database(
    entities = [VehicleLookupRoomEntity::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4, spec = DeleteAtmTuneCacheMigration::class),
    ],
)
@ConstructedBy(VehicleDatabaseConstructor::class)
abstract class VehicleDatabase : RoomDatabase() {
    abstract fun vehicleLookupDao(): VehicleLookupDao
}

@Suppress(
    "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "NO_ACTUAL_FOR_EXPECT",
    "KotlinNoActualForExpect",
)
expect object VehicleDatabaseConstructor : RoomDatabaseConstructor<VehicleDatabase> {
    override fun initialize(): VehicleDatabase
}

internal fun buildVehicleDatabase(builder: Builder<VehicleDatabase>): VehicleDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()

internal expect fun vehicleDatabaseBuilder(): Builder<VehicleDatabase>

@DeleteTable(tableName = "atm_tune_cache")
class DeleteAtmTuneCacheMigration : AutoMigrationSpec

class RoomVehicleLookupStore(
    private val database: VehicleDatabase,
) : VehicleLookupStore {
    private val dao = database.vehicleLookupDao()

    override suspend fun upsert(entity: VehicleLookupEntity) {
        dao.upsert(entity.toRoom())
    }

    override suspend fun getByKenteken(kenteken: String): VehicleLookupEntity? =
        dao.getByKenteken(kenteken)?.toDomain()

    override suspend fun updateLastViewedAt(kenteken: String, at: Long) {
        dao.updateLastViewedAt(kenteken, at)
    }

    override suspend fun updateIsChipped(kenteken: String, isChipped: Boolean) {
        dao.updateIsChipped(kenteken, isChipped)
    }

    override fun observeRecent(limit: Int): Flow<List<VehicleLookupEntity>> =
        dao.observeRecent(limit).map { rows -> rows.map { it.toDomain() } }
}
