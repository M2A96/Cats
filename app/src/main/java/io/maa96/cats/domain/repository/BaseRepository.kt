package io.maa96.cats.domain.repository

import io.maa96.cats.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

abstract class BaseRepository {

    /**
     * This is a generic function for making a network request and caching it into database
     * [ResultType] is type of result that we get from our network request
     * [RequestType] is requesting type of our network request
     * @param query is our database query that we make to get data from database
     * @param fetch is a higher order function that responsible for making our network request
     * @param saveFetchedResult is a higher order function that saves our network response to our database
     * @param shouldFetch is a higher order function that decides whether our response should save in database our not
     * */
    protected inline fun <ResultType, RequestType> networkBoundResource(
        crossinline query: () -> Flow<ResultType>,
        crossinline fetch: suspend () -> RequestType,
        crossinline saveFetchedResult: suspend (RequestType) -> Unit,
        crossinline shouldFetch: (ResultType) -> Boolean = { true }
    ) = flow {
        val data = query().first()
        val flow = if (shouldFetch(data)) {
            this.emit(Resource.Loading(true))
            try {
                saveFetchedResult(fetch())
                query().map { Resource.Success(it) }
            } catch (throwable: Throwable) {
                query().map { Resource.Error(throwable.message!!, it) }
            }
        } else {
            query().map { Resource.Success(it) }
        }
        emitAll(flow)
    }
}
