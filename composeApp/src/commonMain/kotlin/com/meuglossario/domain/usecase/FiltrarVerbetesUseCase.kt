package com.meuglossario.domain.usecase

import com.meuglossario.data.local.currentTimeMillis
import com.meuglossario.domain.model.Verbete
import com.meuglossario.domain.model.VerbeteFiltro
import com.meuglossario.domain.model.VerbeteOrdenacao

/**
 * Aplica busca textual (palavra, definição, observação, referência e tags),
 * filtro e ordenação sobre a lista de verbetes.
 */
class FiltrarVerbetesUseCase {

    operator fun invoke(
        verbetes: List<Verbete>,
        query: String,
        filtro: VerbeteFiltro,
        ordenacao: VerbeteOrdenacao,
        tagSelecionada: String? = null
    ): List<Verbete> {
        val termo = query.trim().lowercase()

        val filtradoBusca = if (termo.isEmpty()) verbetes else verbetes.filter { v ->
            v.palavra.lowercase().contains(termo) ||
                v.definicao.lowercase().contains(termo) ||
                v.observacao.lowercase().contains(termo) ||
                v.referencia.lowercase().contains(termo) ||
                v.tags.any { it.nome.lowercase().contains(termo) }
        }

        val comTag = tagSelecionada?.let { tag ->
            filtradoBusca.filter { v -> v.tags.any { it.nome.equals(tag, ignoreCase = true) } }
        } ?: filtradoBusca

        val comFiltro = when (filtro) {
            VerbeteFiltro.TODOS -> comTag
            VerbeteFiltro.FAVORITOS -> comTag.filter { it.favorito }
            VerbeteFiltro.RECENTES -> {
                val seteDias = 7L * 24 * 60 * 60 * 1000
                val limite = currentTimeMillis() - seteDias
                comTag.filter { it.dataCriacao >= limite }
            }
        }

        return when (ordenacao) {
            VerbeteOrdenacao.ALFABETICA -> comFiltro.sortedBy { it.palavra.lowercase() }
            VerbeteOrdenacao.DATA_CRIACAO -> comFiltro.sortedByDescending { it.dataCriacao }
        }
    }
}
