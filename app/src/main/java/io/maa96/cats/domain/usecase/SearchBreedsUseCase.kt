package io.maa96.cats.domain.usecase

import io.maa96.cats.domain.repository.CatBreedsRepository
import javax.inject.Inject

class SearchBreedsUseCase @Inject constructor(private val catBreedsRepository: CatBreedsRepository) {
    suspend operator fun invoke(query: String, attachImage: Int = 0) =
        catBreedsRepository.searchBreeds(query, attachImage)
}
