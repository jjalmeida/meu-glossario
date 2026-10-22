package com.meuglossario

import com.meuglossario.data.local.LocalDataSource
import com.meuglossario.data.repository.GlossarioRepositoryImpl
import com.meuglossario.data.repository.TagRepositoryImpl
import com.meuglossario.data.repository.VerbeteRepositoryImpl
import com.meuglossario.domain.ValidacaoException
import com.meuglossario.domain.model.Verbete
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RepositoriosTest {

    private fun ambiente(): Triple<GlossarioRepositoryImpl, VerbeteRepositoryImpl, TagRepositoryImpl> {
        val ds = LocalDataSource(InMemoryKeyValueStore())
        val tag = TagRepositoryImpl(ds)
        return Triple(GlossarioRepositoryImpl(ds), VerbeteRepositoryImpl(ds, tag), tag)
    }

    @Test
    fun cria_glossario_valido() = runTest {
        val (g, _, _) = ambiente()
        val id = g.criar("Banco de Dados")
        assertEquals("Banco de Dados", g.buscarPorId(id)?.nome)
    }

    @Test
    fun nome_vazio_falha() = runTest {
        val (g, _, _) = ambiente()
        assertFailsWith<ValidacaoException> { g.criar("   ") }
    }

    @Test
    fun nome_duplicado_falha() = runTest {
        val (g, _, _) = ambiente()
        g.criar("Inglês")
        assertFailsWith<ValidacaoException> { g.criar("inglês") }
    }

    @Test
    fun salvar_verbete_com_tags_e_referencia() = runTest {
        val (g, v, _) = ambiente()
        val gid = g.criar("Tech")
        val id = v.salvar(
            Verbete(glossarioId = gid, palavra = "API", definicao = "Interface", referencia = "RFC"),
            listOf("Programação", "programação") // duplicada -> deduplicada
        )
        val salvo = v.buscarPorId(id)!!
        assertEquals("RFC", salvo.referencia)
        assertEquals(1, salvo.tags.size)
    }

    @Test
    fun excluir_glossario_remove_verbetes() = runTest {
        val (g, v, _) = ambiente()
        val gid = g.criar("X")
        v.salvar(Verbete(glossarioId = gid, palavra = "p", definicao = "d"), emptyList())
        g.excluir(gid)
        assertTrue(v.listar(gid).isEmpty())
    }

    @Test
    fun contagem_total_verbetes() = runTest {
        val (g, v, _) = ambiente()
        val gid = g.criar("X")
        v.salvar(Verbete(glossarioId = gid, palavra = "a", definicao = "d"), emptyList())
        v.salvar(Verbete(glossarioId = gid, palavra = "b", definicao = "d"), emptyList())
        assertEquals(2, v.listar(gid).size)
    }
}
