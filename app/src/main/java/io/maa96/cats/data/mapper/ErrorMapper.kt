package io.maa96.cats.data.mapper

import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

object ErrorMapper {
    fun map(throwable: Throwable): String = when (throwable) {
        is HttpException -> {
            when (throwable.code()) {
                in 400..499 -> mapClientError(throwable.code())
                in 500..599 -> "Server error. Please try again later."
                else -> "An unexpected error occurred."
            }
        }
        is SocketTimeoutException -> "Connection timed out. Please check your internet connection."
        is IOException -> "Network error. Please check your internet connection."
        else -> throwable.message ?: "An unexpected error occurred."
    }

    private fun mapClientError(code: Int): String = when (code) {
        401 -> "Unauthorized. Please login again."
        403 -> "Forbidden!. Turn on VPN! "
        404 -> "Resource not found."
        429 -> "Too many requests. Please try again later."
        else -> "Client error: $code"
    }
}
