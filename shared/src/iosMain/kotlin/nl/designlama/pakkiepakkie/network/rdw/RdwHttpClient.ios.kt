package nl.designlama.pakkiepakkie.network.rdw

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual fun createRdwHttpClient(
    debug: Boolean,
    appToken: String,
    baseUrl: String,
): HttpClient = HttpClient(Darwin) {
    configureRdwClient(debug, appToken, baseUrl)
}
