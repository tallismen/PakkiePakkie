package nl.designlama.pakkiepakkie.network.auth

import io.ktor.client.statement.HttpResponse
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import kotlin.experimental.ExperimentalObjCRefinement

enum class AuthenticationResult {
    Success,
    RequireLogout
}

interface AuthenticationHandler {
    @OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCRefinement::class)
    val tokenStore: TokenStore

    val oauthClient: OpenIdConnectClient

    @OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCRefinement::class)
    val refreshHandler: TokenRefreshHandler

    suspend fun logout()
    fun setUseFullAccessClient(clientId: String)
    suspend fun requestAuthentication(): Result<Unit>
    fun onAuthenticated()
    fun authenticateHeaders(headers: Map<String, String>): Map<String, String>
    suspend fun isAuthenticated(): Boolean
    suspend fun onRequestResult(result: HttpResponse): AuthenticationResult
}
