package io.maa96.cats.presentation.ui.home

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

class SearchDebouncer @Inject constructor() {
    private val _queryFlow = MutableSharedFlow<String>(replay = 1)
    private val debouncedFlow = _queryFlow
        .debounce(1000L)
        .distinctUntilChanged()

    fun updateQuery(query: String) {
        _queryFlow.tryEmit(query)
    }

    fun getQueryFlow(): Flow<String> = debouncedFlow
}
