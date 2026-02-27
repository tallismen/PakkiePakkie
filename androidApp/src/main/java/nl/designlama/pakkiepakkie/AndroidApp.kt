package nl.designlama.pakkiepakkie

import android.app.Application
import nl.designlama.pakkiepakkie.di.AppContext
import nl.designlama.pakkiepakkie.network.NetworkConfig
import nl.designlama.pakkiepakkie.di.commonModule
import nl.designlama.pakkiepakkie.utils.AppConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.HandleRedirectActivity

class AndroidApp : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        AppContext.setApplication(applicationContext)
        configureOidcWebView(javaScriptEnabled = true)
        initKoin()
    }

    @OptIn(ExperimentalOpenIdConnect::class)
    private fun configureOidcWebView(
        javaScriptEnabled: Boolean,
        domStorageEnabled: Boolean = true,
    ) {
        val previous = HandleRedirectActivity.configureWebView
        HandleRedirectActivity.configureWebView = { webView ->
            previous(webView)
            webView.settings.javaScriptEnabled = javaScriptEnabled
            webView.settings.domStorageEnabled = domStorageEnabled
        }
    }

    private fun initKoin() {
        stopKoin()
        startKoin {
            androidContext(this@AndroidApp)
            logger(AndroidLogger())
            modules(
                commonModule(),
                module {
                    single {
                        AppConfig(
                            environment = BuildConfig.ENVIRONMENT,
                            debug = BuildConfig.DEBUG
                        )
                    }
                    single { NetworkConfig(get()) }
                    single<CodeAuthFlowFactory> {
                        AndroidCodeAuthFlowFactory(useWebView = true)
                    }
                }
            )
        }
    }
}
