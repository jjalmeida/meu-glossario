package com.meuglossario

import com.meuglossario.data.local.LocalDataSource
import com.meuglossario.data.repository.GlossarioRepositoryImpl
import com.meuglossario.data.repository.TagRepositoryImpl
import com.meuglossario.data.repository.VerbeteRepositoryImpl
import com.meuglossario.domain.ValidacaoException
import com.meuglossario.domain.repository.GlossarioRepository
import com.meuglossario.domain.repository.VerbeteRepository
import com.meuglossario.domain.usecase.ExportarJsonUseCase
import com.meuglossario.domain.usecase.ImportarJsonUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JsonExportImportTest {

    private class Ambiente {
        val ds = LocalDataSource(InMemoryKeyValueStore())
        val tag = TagRepositoryImpl(ds)
        val glossario: GlossarioRepository = GlossarioRepositoryImpl(ds)
        val verbete: VerbeteRepository = VerbeteRepositoryImpl(ds, tag)
        val importar = ImportarJsonUseCase(glossario, verbete)
        val exportar = ExportarJsonUseCase(glossario, verbete)
    }

    private val jsonValido = """
        {
          "nome": "Banco de Dados",
          "dataCriacao": "2026-01-01",
          "verbetes": [
            { "palavra": "Index", "definicao": "Acelera consultas", "referencia": "cap. 11", "favorito": true, "tags": ["PostgreSQL"] }
          ]
        }
    """.trimIndent()

    @Test
    fun importar_como_novo_cria_glossario() = runTest {
        val a = Ambiente()
        val preview = a.importar.preVisualizar(jsonValido)
        assertEquals("Banco de Dados", preview.dto.nome)
        assertEquals(1, preview.totalVerbetes)

        val id = a.importar.importarComoNovo(preview.dto)
        assertEquals("Banco de Dados", a.glossario.buscarPorId(id)?.nome)
        assertEquals(1, a.verbete.listar(id).size)
        assertEquals("cap. 11", a.verbete.listar(id).first().referencia)
    }

    @Test
    fun importar_como_novo_gera_nome_unico_quando_ja_existe() = runTest {
        val a = Ambiente()
        val dto = a.importar.preVisualizar(jsonValido).dto
        val id1 = a.importar.importarComoNovo(dto)
        val id2 = a.importar.importarComoNovo(dto)
        // Segundo import cria outro glossário, com nome diferente.
        assertTrue(id1 != id2)
        assertEquals("Banco de Dados", a.glossario.buscarPorId(id1)?.nome)
        assertEquals("Banco de Dados (2)", a.glossario.buscarPorId(id2)?.nome)
    }

    @Test
    fun mesclar_em_existente_pula_duplicados() = runTest {
        val a = Ambiente()
        val dto = a.importar.preVisualizar(jsonValido).dto
        val destino = a.glossario.criar("Meu Glossário")
        a.importar.importarMesclando(dto, destino)
        // Mesclar de novo o mesmo conteúdo não deve duplicar "Index".
        a.importar.importarMesclando(dto, destino)
        assertEquals(1, a.verbete.listar(destino).size)
    }

    @Test
    fun json_invalido_falha() = runTest {
        val a = Ambiente()
        assertFailsWith<ValidacaoException> { a.importar.preVisualizar("{ nao é json") }
    }

    @Test
    fun exporta_contem_conteudo() = runTest {
        val a = Ambiente()
        val id = a.importar.importarComoNovo(a.importar.preVisualizar(jsonValido).dto)
        val json = a.exportar(id)
        assertTrue(json.contains("Banco de Dados"))
        assertTrue(json.contains("Index"))
        assertTrue(json.contains("cap. 11"))
    }
}
