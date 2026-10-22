package com.meuglossario.domain.model

/** Filtros disponíveis na tela de verbetes. */
enum class VerbeteFiltro(val rotulo: String) {
    TODOS("Todos"),
    FAVORITOS("Favoritos"),
    RECENTES("Recentes")
}

/** Ordenações disponíveis na tela de verbetes. */
enum class VerbeteOrdenacao(val rotulo: String) {
    ALFABETICA("Ordem alfabética"),
    DATA_CRIACAO("Data de criação")
}
