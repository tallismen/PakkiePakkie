package nl.designlama.pakkiepakkie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nl.designlama.pakkiepakkie.di.AppContext
import org.koin.android.ext.android.inject
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory

class AppActivity : ComponentActivity() {

    val oidcCodeAuthFlowFactory: CodeAuthFlowFactory by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (oidcCodeAuthFlowFactory as? AndroidCodeAuthFlowFactory)?.registerActivity(this)
        enableEdgeToEdge()
        setContent {
            App()
        }
        AppContext.setCurrentActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppContext.clearCurrentActivity(this)
    }
}
