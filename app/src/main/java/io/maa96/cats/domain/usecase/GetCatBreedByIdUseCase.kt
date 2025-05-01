package io.maa96.cats.domain.usecase

import io.maa96.cats.domain.repository.CatBreedsRepository
import javax.inject.Inject

class GetCatBreedByIdUseCase @Inject constructor(private val catBreedsRepository: CatBreedsRepository) {
    suspend operator fun invoke(id: String) = catBreedsRepository.getCatBreedById(id)
}
