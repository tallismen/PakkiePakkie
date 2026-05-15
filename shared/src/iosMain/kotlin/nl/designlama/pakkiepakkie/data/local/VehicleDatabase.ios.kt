package nl.designlama.pakkiepakkie.data.local

import androidx.room.Room
import androidx.room.RoomDatabase.Builder
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private const val VEHICLE_DB_NAME = "pakkie_vehicle.db"

@OptIn(ExperimentalForeignApi::class)
internal actual fun vehicleDatabaseBuilder(): Builder<VehicleDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val dir = requireNotNull(documentDirectory?.path)
    val path = "$dir/$VEHICLE_DB_NAME"
    return Room.databaseBuilder<VehicleDatabase>(name = path)
}
