package nl.designlama.pakkiepakkie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.browser.document
import nl.designlama.pakkiepakkie.app.AppNavigation
import nl.designlama.pakkiepakkie.di.initKoinForWeb
import nl.designlama.pakkiepakkie.firebase.WebFirebaseConfig
import nl.designlama.pakkiepakkie.theme.AppTheme

private val PhoneMaxWidth = 430.dp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    Firebase.initialize(
        context = null,
        options = FirebaseOptions(
            apiKey = WebFirebaseConfig.apiKey,
            authDomain = WebFirebaseConfig.authDomain,
            projectId = WebFirebaseConfig.projectId,
            storageBucket = WebFirebaseConfig.storageBucket,
            gcmSenderId = WebFirebaseConfig.messagingSenderId,
            applicationId = WebFirebaseConfig.appId,
            gaTrackingId = WebFirebaseConfig.measurementId,
        ),
    )
    initKoinForWeb()
    ComposeViewport(document.body!!) {
        AppTheme {
            PhoneFrame {
                AppNavigation()
            }
        }
    }
}

@Composable
private fun PhoneFrame(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = PhoneMaxWidth)
                .fillMaxHeight()
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            content()
        }
    }
}
