package nl.designlama.pakkiepakkie.di

import nl.designlama.pakkiepakkie.data.local.InMemoryVehicleLookupStore
import nl.designlama.pakkiepakkie.data.local.VehicleLookupStore
import nl.designlama.pakkiepakkie.datastore.AppSettings
import nl.designlama.pakkiepakkie.datastore.EncryptedDataStore
import nl.designlama.pakkiepakkie.datastore.WebAppSettings
import nl.designlama.pakkiepakkie.network.NetworkConfig
import nl.designlama.pakkiepakkie.utils.AppConfig
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class PlatformModule actual constructor() {
    actual val module: Module = module {
        single<AppSettings> { WebAppSettings() }
        single<EncryptedDataStore> { EncryptedDataStore() }
        single<VehicleLookupStore> { InMemoryVehicleLookupStore() }
    }
}

fun initKoinForWeb(
    appConfig: AppConfig = AppConfig(
        environment = AppConfig.Environment.PRODUCTION,
        debug = false,
    ),
) {
    stopKoin()
    startKoin {
        modules(
            commonModule(),
            viewModelModule(),
            module {
                single { appConfig }
                single { NetworkConfig(get()) }
            },
        )
    }
}
