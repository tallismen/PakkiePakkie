package nl.designlama.pakkiepakkie.utils

import platform.Foundation.NSLog

actual object Logger {
    actual fun v(tag: String, message: String) {
        NSLog("$tag: $message")
    }
}
