package io.maa96.cats.domain.usecase

import io.maa96.cats.domain.repository.CatBreedsRepository
import javax.inject.Inject

class GetBreedImagesUseCase @Inject constructor(private val catBreedsRepository: CatBreedsRepository) {
    suspend operator fun invoke(breedId: String) =
        catBreedsRepository.getBreedImages(breedId)
}
