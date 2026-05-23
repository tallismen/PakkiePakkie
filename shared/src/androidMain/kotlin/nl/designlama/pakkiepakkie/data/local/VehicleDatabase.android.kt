package nl.designlama.pakkiepakkie.data.local

import androidx.room.Room
import androidx.room.RoomDatabase.Builder
import nl.designlama.pakkiepakkie.di.AppContext

private const val VEHICLE_DB_NAME = "pakkie_vehicle.db"

internal actual fun vehicleDatabaseBuilder(): Builder<VehicleDatabase> {
    val ctx = AppContext.getApplication()
    return Room.databaseBuilder<VehicleDatabase>(
        context = ctx,
        name = ctx.getDatabasePath(VEHICLE_DB_NAME).absolutePath,
    )
}
