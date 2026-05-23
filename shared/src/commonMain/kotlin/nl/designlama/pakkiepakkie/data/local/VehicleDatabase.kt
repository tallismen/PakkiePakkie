package nl.designlama.pakkiepakkie.data.local

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.Builder
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [VehicleLookupEntity::class],
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
