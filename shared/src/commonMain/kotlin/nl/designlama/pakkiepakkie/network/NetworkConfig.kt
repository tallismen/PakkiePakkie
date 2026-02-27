package nl.designlama.pakkiepakkie.network

import nl.designlama.pakkiepakkie.utils.AppConfig
import org.koin.core.annotation.Single

@Single(binds = [NetworkConfig::class])
class NetworkConfig(private val appConfig: AppConfig) {

    val apiBaseUrl: String
        get() = appConfig.apiBaseUrl

    val authBaseUrl: String
        get() = appConfig.authBaseUrl

    val isDebug: Boolean
        get() = appConfig.debug

    val isProduction: Boolean
        get() = appConfig.environment == AppConfig.Environment.PRODUCTION
}
