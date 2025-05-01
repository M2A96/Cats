package io.maa96.cats.util

import javax.inject.Inject

class SecretFields @Inject constructor() {
    private val debugBaseUrl = "https://api.thecatapi.com/"
    private val releaseBaseUrl = "https://api.thecatapi.com/"

    val apiKey = "live_4ugWfKK5r7jzEzxKeav25VgUSE3oICAd5bIJEHeZY7RhKrsQm6leJ8uddfow9rMO"

    //    todo remember to use BuildConfig.DEBUG
    private val isDebug: Boolean = true
    fun getBaseUrl(): String = if (isDebug) {
        debugBaseUrl
    } else {
        releaseBaseUrl
    }
}