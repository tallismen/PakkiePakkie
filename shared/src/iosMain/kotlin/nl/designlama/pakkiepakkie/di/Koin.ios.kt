package nl.designlama.pakkiepakkie.di

import nl.designlama.pakkiepakkie.data.local.buildVehicleDatabase
import nl.designlama.pakkiepakkie.data.local.VehicleDatabase
import nl.designlama.pakkiepakkie.data.local.vehicleDatabaseBuilder
import nl.designlama.pakkiepakkie.datastore.EncryptedDataStore
import nl.designlama.pakkiepakkie.network.NetworkConfig
import nl.designlama.pakkiepakkie.datastore.PrefsDataStore
import nl.designlama.pakkiepakkie.datastore.createDataStore
import nl.designlama.pakkiepakkie.datastore.dataStoreFileName
import nl.designlama.pakkiepakkie.utils.AppConfig
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.IosCodeAuthFlowFactory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class PlatformModule actual constructor() {
    @OptIn(ExperimentalForeignApi::class)
    actual val module: Module = module {
        single<PrefsDataStore> {
            createDataStore(
                producePath = {
                    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                        directory = NSDocumentDirectory,
                        inDomain = NSUserDomainMask,
                        appropriateForURL = null,
                        create = false,
                        error = null,
                    )
                    requireNotNull(documentDirectory).path + "/$dataStoreFileName"
                },
            )
        }

        single<EncryptedDataStore> {
            EncryptedDataStore()
        }

        single<CodeAuthFlowFactory> {
            IosCodeAuthFlowFactory()
        }

        single<VehicleDatabase> {
            buildVehicleDatabase(vehicleDatabaseBuilder())
        }
    }
}

@Suppress("unused")
fun initKoin(appConfig: AppConfig) {
    stopKoin()
    startKoin {
        modules(
            commonModule(),
            viewModelModule(),
            module {
                single { appConfig }
                single { NetworkConfig(get()) }
            }
        )
    }
}
