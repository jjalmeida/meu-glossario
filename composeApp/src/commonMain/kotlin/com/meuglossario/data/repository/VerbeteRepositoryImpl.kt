package com.meuglossario.data.repository

import com.meuglossario.data.local.LocalDataSource
import com.meuglossario.data.local.VerbeteRecord
import com.meuglossario.data.local.currentTimeMillis
import com.meuglossario.domain.ValidacaoException
import com.meuglossario.domain.model.Tag
import com.meuglossario.domain.model.Verbete
import com.meuglossario.domain.repository.VerbeteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VerbeteRepositoryImpl(
    private val dataSource: LocalDataSource,
    private val tagRepository: TagRepositoryImpl
) : VerbeteRepository {

    private fun VerbeteRecord.toDomain(tags: List<Tag>): Verbete = Verbete(
        id = id,
        glossarioId = glossarioId,
        palavra = palavra,
        definicao = definicao,
        observacao = observacao,
        referencia = referencia,
        favorito = favorito,
        dataCriacao = dataCriacao,
        tags = tagIds.mapNotNull { tid -> tags.firstOrNull { it.id == tid } }
    )

    override fun observarVerbetes(glossarioId: Long): Flow<List<Verbete>> =
        dataSource.state.map { state ->
            val tags = state.tags.map { Tag(it.id, it.nome) }
            state.verbetes
                .filter { it.glossarioId == glossarioId }
                .sortedByDescending { it.dataCriacao }
                .map { it.toDomain(tags) }
        }

    override fun observarVerbete(id: Long): Flow<Verbete?> =
        dataSource.state.map { state ->
            val tags = state.tags.map { Tag(it.id, it.nome) }
            state.verbetes.firstOrNull { it.id == id }?.toDomain(tags)
        }

    override suspend fun buscarPorId(id: Long): Verbete? {
        val state = dataSource.snapshot()
        val tags = state.tags.map { Tag(it.id, it.nome) }
        return state.verbetes.firstOrNull { it.id == id }?.toDomain(tags)
    }

    override suspend fun listar(glossarioId: Long): List<Verbete> {
        val state = dataSource.snapshot()
        val tags = state.tags.map { Tag(it.id, it.nome) }
        return state.verbetes.filter { it.glossarioId == glossarioId }.map { it.toDomain(tags) }
    }

    override suspend fun salvar(verbete: Verbete, nomesTags: List<String>): Long {
        if (verbete.palavra.trim().isEmpty()) throw ValidacaoException("A palavra é obrigatória.")
        if (verbete.definicao.trim().isEmpty()) throw ValidacaoException("A definição é obrigatória.")

        val tagIds = tagRepository.resolverTags(
            nomesTags.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
        )

        return if (verbete.id == 0L) {
            val novoId = dataSource.snapshot().seqVerbete + 1
            dataSource.update { s ->
                s.copy(
                    verbetes = s.verbetes + VerbeteRecord(
                        id = novoId,
                        glossarioId = verbete.glossarioId,
                        palavra = verbete.palavra.trim(),
                        definicao = verbete.definicao.trim(),
                        observacao = verbete.observacao.trim(),
                        referencia = verbete.referencia.trim(),
                        favorito = verbete.favorito,
                        dataCriacao = if (verbete.dataCriacao == 0L) currentTimeMillis() else verbete.dataCriacao,
                        tagIds = tagIds
                    ),
                    seqVerbete = novoId
                )
            }
            novoId
        } else {
            dataSource.update { s ->
                s.copy(verbetes = s.verbetes.map { r ->
                    if (r.id == verbete.id) r.copy(
                        palavra = verbete.palavra.trim(),
                        definicao = verbete.definicao.trim(),
                        observacao = verbete.observacao.trim(),
                        referencia = verbete.referencia.trim(),
                        favorito = verbete.favorito,
                        tagIds = tagIds
                    ) else r
                })
            }
            verbete.id
        }
    }

    override suspend fun excluir(id: Long) {
        dataSource.update { s -> s.copy(verbetes = s.verbetes.filterNot { it.id == id }) }
    }

    override suspend fun definirFavorito(id: Long, favorito: Boolean) {
        dataSource.update { s ->
            s.copy(verbetes = s.verbetes.map { if (it.id == id) it.copy(favorito = favorito) else it })
        }
    }

    override fun observarTotalVerbetes(): Flow<Int> =
        dataSource.state.map { it.verbetes.size }

    override fun observarTotalFavoritos(): Flow<Int> =
        dataSource.state.map { state -> state.verbetes.count { it.favorito } }

    override fun observarTotalPorGlossario(glossarioId: Long): Flow<Int> =
        dataSource.state.map { state -> state.verbetes.count { it.glossarioId == glossarioId } }

    override fun observarFavoritosPorGlossario(glossarioId: Long): Flow<Int> =
        dataSource.state.map { state -> state.verbetes.count { it.glossarioId == glossarioId && it.favorito } }

    override suspend fun dataUltimoVerbete(): Long? =
        dataSource.snapshot().verbetes.maxByOrNull { it.dataCriacao }?.dataCriacao
}
