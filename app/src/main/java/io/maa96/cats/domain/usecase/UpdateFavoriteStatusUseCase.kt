package io.maa96.cats.domain.usecase

import io.maa96.cats.domain.model.Cat
import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.repository.CatBreedsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateFavoriteStatusUseCase @Inject constructor(
    private val repository: CatBreedsRepository
) {
    suspend operator fun invoke(breed: Cat): Flow<Resource<Boolean>> {
        return repository.updateFavoriteStatus(breed)
    }
}