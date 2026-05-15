package nl.designlama.pakkiepakkie.network.rdw

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createRdwHttpClient(
    debug: Boolean,
    appToken: String,
    baseUrl: String,
): HttpClient = HttpClient(OkHttp) {
    configureRdwClient(debug, appToken, baseUrl)
}
