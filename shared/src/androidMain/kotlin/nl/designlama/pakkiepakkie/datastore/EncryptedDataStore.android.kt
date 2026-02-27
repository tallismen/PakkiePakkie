package nl.designlama.pakkiepakkie.datastore

import nl.designlama.pakkiepakkie.di.AppContext
import eu.anifantakis.lib.ksafe.KSafe

actual class EncryptedDataStore {

    private val kSafe = KSafe(AppContext.getApplication())

    actual fun getVault(): KSafe {
        return kSafe
    }
}
