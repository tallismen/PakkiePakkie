package nl.designlama.pakkiepakkie.utils

import PakkiePakkie.shared.BuildConfig

data class AppConfig(
    val environment: Environment,
    val debug: Boolean
) {

    enum class Environment {
        DEVELOP, STAGING, PRODUCTION
    }

    val apiBaseUrl: String
        get() = when (environment) {
            Environment.DEVELOP -> BuildConfig.BASE_URL_API_DEV
            Environment.STAGING -> BuildConfig.BASE_URL_API_STAG
            Environment.PRODUCTION -> BuildConfig.BASE_URL_API_PRD
        }

    val authBaseUrl: String
        get() = when (environment) {
            Environment.DEVELOP -> BuildConfig.BASE_URL_AUTH_DEV
            Environment.STAGING -> BuildConfig.BASE_URL_AUTH_STAG
            Environment.PRODUCTION -> BuildConfig.BASE_URL_AUTH_PRD
        }
}
