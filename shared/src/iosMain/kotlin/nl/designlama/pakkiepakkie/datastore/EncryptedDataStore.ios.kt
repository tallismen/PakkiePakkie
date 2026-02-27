package nl.designlama.pakkiepakkie.datastore

import eu.anifantakis.lib.ksafe.KSafe

actual class EncryptedDataStore {

    private val kSafe = KSafe()

    actual fun getVault(): KSafe {
        return kSafe
    }
}
