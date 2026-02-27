package nl.designlama.pakkiepakkie.datastore

import eu.anifantakis.lib.ksafe.KSafe

expect class EncryptedDataStore {
    fun getVault(): KSafe
}

object EncryptedDataKey {
    const val ACCESS_TOKEN_EXPIRY_TIME = "access_token_expiry_time"
}
