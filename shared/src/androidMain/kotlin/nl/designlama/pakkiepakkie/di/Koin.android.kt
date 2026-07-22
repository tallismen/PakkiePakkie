package nl.designlama.pakkiepakkie.di

import nl.designlama.pakkiepakkie.data.local.RoomVehicleLookupStore
import nl.designlama.pakkiepakkie.data.local.VehicleDatabase
import nl.designlama.pakkiepakkie.data.local.VehicleLookupStore
import nl.designlama.pakkiepakkie.data.local.buildVehicleDatabase
import nl.designlama.pakkiepakkie.data.local.vehicleDatabaseBuilder
import nl.designlama.pakkiepakkie.datastore.AppSettings
import nl.designlama.pakkiepakkie.datastore.DataStoreAppSettings
import nl.designlama.pakkiepakkie.datastore.EncryptedDataStore
import nl.designlama.pakkiepakkie.datastore.createDataStore
import nl.designlama.pakkiepakkie.datastore.dataStoreFileName
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class PlatformModule actual constructor() {
    actual val module: Module = module {
        single<AppSettings> {
            DataStoreAppSettings(
                createDataStore(
                    producePath = {
                        androidContext().filesDir.resolve(dataStoreFileName).absolutePath
                    },
                ),
            )
        }

        single<EncryptedDataStore> {
            EncryptedDataStore()
        }

        single<VehicleDatabase> {
            buildVehicleDatabase(vehicleDatabaseBuilder())
        }

        single<VehicleLookupStore> {
            RoomVehicleLookupStore(get())
        }
    }
}
