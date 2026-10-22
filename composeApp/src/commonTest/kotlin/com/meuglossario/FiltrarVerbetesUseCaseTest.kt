package com.meuglossario

import com.meuglossario.data.local.currentTimeMillis
import com.meuglossario.domain.model.Tag
import com.meuglossario.domain.model.Verbete
import com.meuglossario.domain.model.VerbeteFiltro
import com.meuglossario.domain.model.VerbeteOrdenacao
import com.meuglossario.domain.usecase.FiltrarVerbetesUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FiltrarVerbetesUseCaseTest {

    private val useCase = FiltrarVerbetesUseCase()
    private val agora = currentTimeMillis()
    private val antigo = agora - 30L * 24 * 60 * 60 * 1000

    private val verbetes = listOf(
        Verbete(1, 1, "Index", "Acelera consultas", "banco", "Manual do PostgreSQL", true, agora, listOf(Tag(1, "PostgreSQL"))),
        Verbete(2, 1, "Cache", "Armazena temporário", "", "", false, antigo, listOf(Tag(2, "Performance"))),
        Verbete(3, 1, "Array", "Coleção ordenada", "estrutura", "", false, agora, emptyList())
    )

    @Test
    fun busca_na_palavra() {
        val r = useCase(verbetes, "index", VerbeteFiltro.TODOS, VerbeteOrdenacao.DATA_CRIACAO)
        assertEquals(1, r.size)
        assertEquals("Index", r.first().palavra)
    }

    @Test
    fun busca_na_referencia() {
        val r = useCase(verbetes, "manual", VerbeteFiltro.TODOS, VerbeteOrdenacao.DATA_CRIACAO)
        assertEquals("Index", r.single().palavra)
    }

    @Test
    fun busca_nas_tags() {
        val r = useCase(verbetes, "performance", VerbeteFiltro.TODOS, VerbeteOrdenacao.DATA_CRIACAO)
        assertEquals("Cache", r.single().palavra)
    }

    @Test
    fun filtro_favoritos() {
        val r = useCase(verbetes, "", VerbeteFiltro.FAVORITOS, VerbeteOrdenacao.DATA_CRIACAO)
        assertTrue(r.all { it.favorito })
        assertEquals(1, r.size)
    }

    @Test
    fun ordenacao_alfabetica() {
        val r = useCase(verbetes, "", VerbeteFiltro.TODOS, VerbeteOrdenacao.ALFABETICA)
        assertEquals(listOf("Array", "Cache", "Index"), r.map { it.palavra })
    }

    @Test
    fun filtro_por_tag() {
        val r = useCase(verbetes, "", VerbeteFiltro.TODOS, VerbeteOrdenacao.DATA_CRIACAO, tagSelecionada = "Performance")
        assertEquals("Cache", r.single().palavra)
    }
}
