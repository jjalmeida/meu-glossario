package com.meuglossario.domain.usecase

import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.repository.GlossarioRepository
import kotlinx.coroutines.flow.Flow

class ObservarGlossariosUseCase(private val repository: GlossarioRepository) {
    operator fun invoke(): Flow<List<Glossario>> = repository.observarGlossarios()
}

class ObservarGlossarioUseCase(private val repository: GlossarioRepository) {
    operator fun invoke(id: Long): Flow<Glossario?> = repository.observarGlossario(id)
}

class CriarGlossarioUseCase(private val repository: GlossarioRepository) {
    suspend operator fun invoke(nome: String): Long = repository.criar(nome)
}

class RenomearGlossarioUseCase(private val repository: GlossarioRepository) {
    suspend operator fun invoke(id: Long, novoNome: String) = repository.renomear(id, novoNome)
}

class ExcluirGlossarioUseCase(private val repository: GlossarioRepository) {
    suspend operator fun invoke(id: Long) = repository.excluir(id)
}
