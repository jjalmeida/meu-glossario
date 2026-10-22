package com.meuglossario.domain.model

/** Agrupa os dados exibidos na tela de estatísticas. */
data class Estatisticas(
    val totalGlossarios: Int = 0,
    val totalVerbetes: Int = 0,
    val totalFavoritos: Int = 0,
    val contagemPorTag: List<ContagemTag> = emptyList(),
    val glossarioComMaisVerbetes: Glossario? = null,
    val dataUltimoVerbete: Long? = null
)

data class ContagemTag(
    val nome: String,
    val total: Int
)
