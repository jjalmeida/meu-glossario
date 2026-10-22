package com.meuglossario.data.repository

import com.meuglossario.data.local.GlossarioRecord
import com.meuglossario.data.local.LocalDataSource
import com.meuglossario.data.local.currentTimeMillis
import com.meuglossario.domain.ValidacaoException
import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.repository.GlossarioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GlossarioRepositoryImpl(
    private val dataSource: LocalDataSource
) : GlossarioRepository {

    override fun observarGlossarios(): Flow<List<Glossario>> =
        dataSource.state.map { state ->
            state.glossarios
                .sortedBy { it.nome.lowercase() }
                .map { g ->
                    Glossario(
                        id = g.id,
                        nome = g.nome,
                        dataCriacao = g.dataCriacao,
                        totalVerbetes = state.verbetes.count { it.glossarioId == g.id }
                    )
                }
        }

    override fun observarGlossario(id: Long): Flow<Glossario?> =
        dataSource.state.map { state ->
            state.glossarios.firstOrNull { it.id == id }?.let { g ->
                Glossario(g.id, g.nome, g.dataCriacao, state.verbetes.count { it.glossarioId == g.id })
            }
        }

    override suspend fun buscarPorId(id: Long): Glossario? {
        val state = dataSource.snapshot()
        return state.glossarios.firstOrNull { it.id == id }?.let { g ->
            Glossario(g.id, g.nome, g.dataCriacao, state.verbetes.count { it.glossarioId == g.id })
        }
    }

    override suspend fun criar(nome: String): Long {
        val limpo = nome.trim()
        if (limpo.isEmpty()) throw ValidacaoException("O nome do glossário é obrigatório.")
        if (dataSource.snapshot().glossarios.any { it.nome.equals(limpo, ignoreCase = true) }) {
            throw ValidacaoException("Já existe um glossário com esse nome.")
        }
        val novoId = dataSource.snapshot().seqGlossario + 1
        dataSource.update { s ->
            s.copy(
                glossarios = s.glossarios + GlossarioRecord(novoId, limpo, currentTimeMillis()),
                seqGlossario = novoId
            )
        }
        return novoId
    }

    override suspend fun renomear(id: Long, novoNome: String) {
        val limpo = novoNome.trim()
        if (limpo.isEmpty()) throw ValidacaoException("O nome do glossário é obrigatório.")
        val state = dataSource.snapshot()
        if (state.glossarios.any { it.nome.equals(limpo, ignoreCase = true) && it.id != id }) {
            throw ValidacaoException("Já existe um glossário com esse nome.")
        }
        if (state.glossarios.none { it.id == id }) {
            throw ValidacaoException("Glossário não encontrado.")
        }
        dataSource.update { s ->
            s.copy(glossarios = s.glossarios.map { if (it.id == id) it.copy(nome = limpo) else it })
        }
    }

    override suspend fun excluir(id: Long) {
        dataSource.update { s ->
            val verbetesRestantes = s.verbetes.filterNot { it.glossarioId == id }
            s.copy(
                glossarios = s.glossarios.filterNot { it.id == id },
                verbetes = verbetesRestantes
            )
        }
    }

    override suspend fun contarGlossarios(): Int = dataSource.snapshot().glossarios.size

    override suspend fun idPorNome(nome: String): Long? =
        dataSource.snapshot().glossarios
            .firstOrNull { it.nome.equals(nome.trim(), ignoreCase = true) }?.id
}
