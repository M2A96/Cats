package io.maa96.cats.domain.usecase

import io.maa96.cats.domain.repository.CatBreedsRepository
import javax.inject.Inject

class GetCatBreedsUseCase @Inject constructor(
    private val catBreedsRepository: CatBreedsRepository
) {
    suspend operator fun invoke(limit: Int, page: Int) = catBreedsRepository.getCatBreeds(limit, page)
}