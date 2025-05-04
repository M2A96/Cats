package io.maa96.cats.presentation.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

fun <T> MutableStateFlow<T>.updateState(transform: (T) -> T) {
    this.update(transform)
}
