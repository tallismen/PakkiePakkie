package nl.designlama.pakkiepakkie.di

import nl.designlama.pakkiepakkie.datastore.EncryptedDataStore
import nl.designlama.pakkiepakkie.datastore.PrefsDataStore
import nl.designlama.pakkiepakkie.datastore.createDataStore
import nl.designlama.pakkiepakkie.datastore.dataStoreFileName
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class PlatformModule actual constructor() {
    actual val module: Module = module {
        single<PrefsDataStore> {
            createDataStore(
                producePath = {
                    androidContext().filesDir.resolve(dataStoreFileName).absolutePath
                },
            )
        }

        single<EncryptedDataStore> {
            EncryptedDataStore()
        }
    }
}
