package io.maa96.cats.presentation.ui.home

import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce

class SearchDebouncer @Inject constructor() {
    private val queryFlow = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    fun getQueryFlow(
        debounceMs: Long = 1000
    ): Flow<String> = queryFlow.debounce(debounceMs)

    fun setQuery(query: String) {
        queryFlow.value = query
    }
}