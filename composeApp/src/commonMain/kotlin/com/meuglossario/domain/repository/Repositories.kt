package com.meuglossario.domain.repository

import com.meuglossario.domain.model.ContagemTag
import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.model.Tag
import com.meuglossario.domain.model.Verbete
import kotlinx.coroutines.flow.Flow

interface GlossarioRepository {
    fun observarGlossarios(): Flow<List<Glossario>>
    fun observarGlossario(id: Long): Flow<Glossario?>
    suspend fun buscarPorId(id: Long): Glossario?
    suspend fun criar(nome: String): Long
    suspend fun renomear(id: Long, novoNome: String)
    suspend fun excluir(id: Long)
    suspend fun contarGlossarios(): Int
    suspend fun idPorNome(nome: String): Long?
}

interface VerbeteRepository {
    fun observarVerbetes(glossarioId: Long): Flow<List<Verbete>>
    fun observarVerbete(id: Long): Flow<Verbete?>
    suspend fun buscarPorId(id: Long): Verbete?
    suspend fun listar(glossarioId: Long): List<Verbete>
    suspend fun salvar(verbete: Verbete, nomesTags: List<String>): Long
    suspend fun excluir(id: Long)
    suspend fun definirFavorito(id: Long, favorito: Boolean)
    fun observarTotalVerbetes(): Flow<Int>
    fun observarTotalFavoritos(): Flow<Int>
    fun observarTotalPorGlossario(glossarioId: Long): Flow<Int>
    fun observarFavoritosPorGlossario(glossarioId: Long): Flow<Int>
    suspend fun dataUltimoVerbete(): Long?
}

interface TagRepository {
    fun observarTags(): Flow<List<Tag>>
    suspend fun criar(nome: String): Long
    suspend fun renomear(id: Long, novoNome: String)
    suspend fun excluir(id: Long)
    fun observarContagemPorTag(): Flow<List<ContagemTag>>
}
