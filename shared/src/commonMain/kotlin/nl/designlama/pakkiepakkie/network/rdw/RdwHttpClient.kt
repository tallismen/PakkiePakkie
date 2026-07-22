package nl.designlama.pakkiepakkie.network.rdw

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger as KtorHttpLogger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

import nl.designlama.pakkiepakkie.utils.Logger

private val appTokenHeaderRegex = Regex("(${Regex.escape(RdwApi.HEADER_APP_TOKEN)}:\\s*)\\S+", RegexOption.IGNORE_CASE)

internal fun sanitizeRdwHttpLog(message: String): String =
    message.replace(appTokenHeaderRegex, "$1***")

internal fun HttpClientConfig<*>.configureRdwClient(
    debug: Boolean,
    appToken: String,
    baseUrl: String,
) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            },
        )
    }
    defaultRequest {
        url(baseUrl)
        if (appToken.isNotEmpty()) {
            header(RdwApi.HEADER_APP_TOKEN, appToken)
        }
    }
    if (debug) {
        install(Logging) {
            level = LogLevel.BODY
            logger = object : KtorHttpLogger {
                override fun log(message: String) {
                    Logger.v("RdwHttp", sanitizeRdwHttpLog(message))
                }
            }
        }
    }
}

internal expect fun createRdwHttpClient(
    debug: Boolean,
    appToken: String,
    baseUrl: String,
): HttpClient
