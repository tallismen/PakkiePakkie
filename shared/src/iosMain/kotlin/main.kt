import androidx.compose.ui.window.ComposeUIViewController
import nl.designlama.pakkiepakkie.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
