package nl.designlama.pakkiepakkie.network.auth

import nl.designlama.pakkiepakkie.datastore.EncryptedDataStore
import nl.designlama.pakkiepakkie.network.NetworkConfig
import nl.designlama.pakkiepakkie.utils.LogTag
import nl.designlama.pakkiepakkie.utils.Logger
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.tokenstore.SettingsTokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.removeTokens
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AuthenticationHandlerModern @OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCRefinement::class) constructor(
    val encryptedDataStore: EncryptedDataStore,
    private val config: NetworkConfig,
    private val authFlowFactory: CodeAuthFlowFactory,
): AuthenticationHandler {

    // TODO: Replace with your OIDC client configuration
    private var enrollClientId = "pakkiepakkieapp.enroll"
    private val enrollClientScopes = "openid profile offline_access"
    private val authenticatedDeviceScopes = "openid offline_access"
    private val baseUrl = config.authBaseUrl

    private val oauthTokenStore = OAuthTokenStore(encryptedDataStore)
    private var isAuthenticating = false
    private var tokenRefreshJob: Job? = null

    override var oauthClient = OpenIdConnectClient(
        discoveryUri = "${baseUrl}/.well-known/openid-configuration") {
        clientId = enrollClientId
        clientSecret = ""
        scope = enrollClientScopes
        codeChallengeMethod = CodeChallengeMethod.S256
        redirectUri = LOGGED_IN_URI
        postLogoutRedirectUri = LOGGED_OUT_URI
    }

    @OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCRefinement::class)
    override val tokenStore = SettingsTokenStore(oauthTokenStore)

    @OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCRefinement::class)
    override val refreshHandler = TokenRefreshHandler(tokenStore = tokenStore)

    @OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCRefinement::class)
    override suspend fun logout() {
        val idToken = tokenStore.getIdToken() ?: return
        val logoutFlow = authFlowFactory.createEndSessionFlow(oauthClient)
        logoutFlow.startLogout(idToken)
        oauthTokenStore.clear()
        tokenStore.removeTokens()
    }

    override fun setUseFullAccessClient(clientId: String) {
        oauthClient = OpenIdConnectClient(
            discoveryUri = "${baseUrl}/.well-known/openid-configuration") {
            this.clientId = clientId
            clientSecret = ""
            scope = authenticatedDeviceScopes
            codeChallengeMethod = CodeChallengeMethod.S256
            redirectUri = LOGGED_IN_URI
            postLogoutRedirectUri = LOGGED_OUT_URI
        }
    }

    @OptIn(ExperimentalOpenIdConnect::class, ExperimentalTime::class)
    override suspend fun isAuthenticated(): Boolean {
        val expiryTime = oauthTokenStore.getAccessTokenExpiryTime() ?: return false
        return expiryTime >= Clock.System.now().toEpochMilliseconds()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun requestAuthentication(): Result<Unit> {
        try {
            isAuthenticating = true
            oauthClient.discover()
            val flow = authFlowFactory.createAuthFlow(oauthClient)
            val tokens = flow.getAccessToken()
            persistTokens(tokens)
            oauthTokenStore.setAccessTokenExpiryTime(
                Clock.System.now().toEpochMilliseconds() +
                        ((tokens.expires_in?.toLong() ?: 0L) * 1000))
            isAuthenticating = false
            if (tokenRefreshJob == null) {
                startTokenRefreshJob()
            }
            return Result.success(Unit)
        } catch (error: Exception) {
            isAuthenticating = false
            return Result.failure(error)
        }
    }

    override fun onAuthenticated() {}

    override fun authenticateHeaders(headers: Map<String, String>): Map<String, String> {
        return headers
    }

    override suspend fun onRequestResult(result: HttpResponse): AuthenticationResult {
        return if (result.status == HttpStatusCode.Unauthorized || result.status == HttpStatusCode.Forbidden) {
            AuthenticationResult.RequireLogout
        } else {
            AuthenticationResult.Success
        }
    }

    private fun startTokenRefreshJob() {
        Logger.v(LogTag.DEFAULT, "Starting token refresh job")
        tokenRefreshJob = startAuthTokenPeriodicRefresh(1000L) {
            refreshTokenIfNeeded()
        }
    }

    @OptIn(ExperimentalOpenIdConnect::class, ExperimentalObjCRefinement::class)
    private suspend fun persistTokens(tokens: AccessTokenResponse) {
        tokenStore.saveTokens(tokens.access_token, tokens.refresh_token, tokens.id_token)
    }

    private fun startAuthTokenPeriodicRefresh(
        intervalMs: Long,
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
        block: suspend () -> Unit
    ): Job {
        return coroutineScope.launch {
            while (coroutineContext.isActive) {
                block()
                delay(intervalMs)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun refreshTokenIfNeeded() {
        val expireTime = oauthTokenStore.getAccessTokenExpiryTime() ?: return
        val timeLeft = expireTime - Clock.System.now().toEpochMilliseconds()
        if (timeLeft >= 60000 || isAuthenticating) {
            return
        }
        requestAuthentication()
    }

    companion object {
        const val LOGGED_IN_URI = "pakkiepakkieapp://authorize-callback"
        const val LOGGED_OUT_URI = "pakkiepakkieapp://loggedout-callback"
    }
}
