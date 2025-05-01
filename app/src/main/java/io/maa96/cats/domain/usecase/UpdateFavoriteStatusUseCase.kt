package io.maa96.cats.domain.usecase

import io.maa96.cats.domain.model.Resource
import io.maa96.cats.domain.repository.CatBreedsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class UpdateFavoriteStatusUseCase @Inject constructor(private val repository: CatBreedsRepository) {
    suspend operator fun invoke(breedId: String, isFav: Boolean):
        Flow<Resource<Boolean>> = repository.updateFavoriteStatus(breedId, isFav)
}
