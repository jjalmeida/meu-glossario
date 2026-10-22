package com.meuglossario.domain.usecase

import com.meuglossario.domain.model.Estatisticas
import com.meuglossario.domain.repository.GlossarioRepository
import com.meuglossario.domain.repository.TagRepository
import com.meuglossario.domain.repository.VerbeteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class ObterEstatisticasUseCase(
    private val glossarioRepository: GlossarioRepository,
    private val verbeteRepository: VerbeteRepository,
    private val tagRepository: TagRepository
) {
    operator fun invoke(): Flow<Estatisticas> = combine(
        glossarioRepository.observarGlossarios(),
        verbeteRepository.observarTotalVerbetes(),
        verbeteRepository.observarTotalFavoritos(),
        tagRepository.observarContagemPorTag(),
        ultimaDataFlow()
    ) { glossarios, totalVerbetes, totalFavoritos, contagemTags, ultimaData ->
        Estatisticas(
            totalGlossarios = glossarios.size,
            totalVerbetes = totalVerbetes,
            totalFavoritos = totalFavoritos,
            contagemPorTag = contagemTags,
            glossarioComMaisVerbetes = glossarios.maxByOrNull { it.totalVerbetes },
            dataUltimoVerbete = ultimaData
        )
    }

    private fun ultimaDataFlow(): Flow<Long?> = flow {
        emit(verbeteRepository.dataUltimoVerbete())
    }
}
