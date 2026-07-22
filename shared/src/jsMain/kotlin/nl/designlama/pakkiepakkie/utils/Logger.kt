package nl.designlama.pakkiepakkie.utils

actual object Logger {
    actual fun v(tag: String, message: String) {
        println("[$tag] $message")
    }
}
