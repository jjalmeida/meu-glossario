package com.meuglossario.data.repository

import com.meuglossario.data.local.LocalDataSource
import com.meuglossario.data.local.TagRecord
import com.meuglossario.domain.ValidacaoException
import com.meuglossario.domain.model.ContagemTag
import com.meuglossario.domain.model.Tag
import com.meuglossario.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TagRepositoryImpl(
    private val dataSource: LocalDataSource
) : TagRepository {

    override fun observarTags(): Flow<List<Tag>> =
        dataSource.state.map { state ->
            state.tags.sortedBy { it.nome.lowercase() }.map { Tag(it.id, it.nome) }
        }

    override suspend fun criar(nome: String): Long = obterOuCriar(nome)

    /** Cria a tag caso não exista (case-insensitive) e retorna o id. */
    private fun obterOuCriar(nome: String): Long {
        val limpo = nome.trim()
        if (limpo.isEmpty()) throw ValidacaoException("O nome da tag é obrigatório.")
        val existente = dataSource.snapshot().tags.firstOrNull { it.nome.equals(limpo, ignoreCase = true) }
        if (existente != null) return existente.id
        val novoId = dataSource.snapshot().seqTag + 1
        dataSource.update { s ->
            s.copy(tags = s.tags + TagRecord(novoId, limpo), seqTag = novoId)
        }
        return novoId
    }

    override suspend fun renomear(id: Long, novoNome: String) {
        val limpo = novoNome.trim()
        if (limpo.isEmpty()) throw ValidacaoException("O nome da tag é obrigatório.")
        if (dataSource.snapshot().tags.any { it.nome.equals(limpo, ignoreCase = true) && it.id != id }) {
            throw ValidacaoException("Já existe uma tag com esse nome.")
        }
        dataSource.update { s ->
            s.copy(tags = s.tags.map { if (it.id == id) it.copy(nome = limpo) else it })
        }
    }

    override suspend fun excluir(id: Long) {
        dataSource.update { s ->
            s.copy(
                tags = s.tags.filterNot { it.id == id },
                verbetes = s.verbetes.map { v -> v.copy(tagIds = v.tagIds.filterNot { it == id }) }
            )
        }
    }

    override fun observarContagemPorTag(): Flow<List<ContagemTag>> =
        dataSource.state.map { state ->
            state.tags
                .map { tag ->
                    ContagemTag(tag.nome, state.verbetes.count { tag.id in it.tagIds })
                }
                .sortedWith(compareByDescending<ContagemTag> { it.total }.thenBy { it.nome.lowercase() })
        }

    /** Usado pelo repositório de verbetes para resolver nomes de tags em ids. */
    fun resolverTags(nomes: List<String>): List<Long> =
        nomes.map { obterOuCriar(it) }
}
