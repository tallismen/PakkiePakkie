package nl.designlama.pakkiepakkie.network.auth

import nl.designlama.pakkiepakkie.datastore.EncryptedDataKey.ACCESS_TOKEN_EXPIRY_TIME
import nl.designlama.pakkiepakkie.datastore.EncryptedDataStore
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.tokenstore.SettingsKey
import org.publicvalue.multiplatform.oidc.tokenstore.SettingsStore
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCRefinement::class)
@HiddenFromObjC
class OAuthTokenStore(
    val encryptedDataStore: EncryptedDataStore
): SettingsStore {

    suspend fun getAccessTokenExpiryTime(): Long? = encryptedDataStore.getVault().get(ACCESS_TOKEN_EXPIRY_TIME, null)

    suspend fun setAccessTokenExpiryTime(expiryTime: Long) {
        encryptedDataStore.getVault().put(ACCESS_TOKEN_EXPIRY_TIME, expiryTime)
    }

    override suspend fun get(key: String): String? {
        return encryptedDataStore.getVault().get(key, null)
    }

    override suspend fun put(key: String, value: String) {
        encryptedDataStore.getVault().put(key, value)
    }

    override suspend fun remove(key: String) {
        encryptedDataStore.getVault().delete(key)
    }

    override suspend fun clear() {
        encryptedDataStore.getVault().delete(SettingsKey.REFRESHTOKEN.name)
        encryptedDataStore.getVault().delete(SettingsKey.ACCESSTOKEN.name)
        encryptedDataStore.getVault().delete(SettingsKey.IDTOKEN.name)
        encryptedDataStore.getVault().delete(ACCESS_TOKEN_EXPIRY_TIME)
    }
}
