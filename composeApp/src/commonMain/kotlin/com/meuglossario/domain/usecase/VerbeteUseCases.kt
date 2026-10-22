package com.meuglossario.domain.usecase

import com.meuglossario.domain.model.Verbete
import com.meuglossario.domain.repository.VerbeteRepository
import kotlinx.coroutines.flow.Flow

class ObservarVerbetesUseCase(private val repository: VerbeteRepository) {
    operator fun invoke(glossarioId: Long): Flow<List<Verbete>> = repository.observarVerbetes(glossarioId)
}

class ObservarVerbeteUseCase(private val repository: VerbeteRepository) {
    operator fun invoke(id: Long): Flow<Verbete?> = repository.observarVerbete(id)
}

class SalvarVerbeteUseCase(private val repository: VerbeteRepository) {
    suspend operator fun invoke(verbete: Verbete, nomesTags: List<String>): Long =
        repository.salvar(verbete, nomesTags)
}

class ExcluirVerbeteUseCase(private val repository: VerbeteRepository) {
    suspend operator fun invoke(id: Long) = repository.excluir(id)
}

class AlternarFavoritoUseCase(private val repository: VerbeteRepository) {
    suspend operator fun invoke(id: Long, favorito: Boolean) = repository.definirFavorito(id, favorito)
}
