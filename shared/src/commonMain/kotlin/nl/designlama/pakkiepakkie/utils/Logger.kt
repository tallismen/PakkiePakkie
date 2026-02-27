package nl.designlama.pakkiepakkie.utils

expect object Logger {
    fun v(tag: String, message: String)
}

object LogTag {
    const val DEFAULT = "PakkiePakkie"
}
