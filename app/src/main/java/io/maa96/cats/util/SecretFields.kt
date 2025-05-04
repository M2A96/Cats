package io.maa96.cats.util

import io.maa96.cats.BuildConfig
import javax.inject.Inject

class SecretFields @Inject constructor() {
    private val debugBaseUrl = "https://api.thecatapi.com/"
    private val releaseBaseUrl = "https://api.thecatapi.com/"

    val apiKey = BuildConfig.CAT_API_KEY

    fun getBaseUrl(): String = if (BuildConfig.DEBUG) {
        debugBaseUrl
    } else {
        releaseBaseUrl
    }
}
