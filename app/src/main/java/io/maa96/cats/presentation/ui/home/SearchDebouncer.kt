package io.maa96.cats.presentation.ui.home

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

class SearchDebouncer @Inject constructor() {
    private val _queryFlow = MutableStateFlow("")

    private val debouncedFlow = _queryFlow
        .debounce(500L) // Reduced debounce time for better UX
        .distinctUntilChanged()

    fun updateQuery(query: String) {
        _queryFlow.value = query // Use value instead of tryEmit for guaranteed delivery
    }

    fun getQueryFlow(): Flow<String> = debouncedFlow
}
