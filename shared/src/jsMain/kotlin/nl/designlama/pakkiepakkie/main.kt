package nl.designlama.pakkiepakkie

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.browser.document
import nl.designlama.pakkiepakkie.di.initKoinForWeb
import nl.designlama.pakkiepakkie.firebase.WebFirebaseConfig

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
        App()
    }
}
