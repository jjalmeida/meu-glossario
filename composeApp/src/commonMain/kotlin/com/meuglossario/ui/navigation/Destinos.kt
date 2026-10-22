package com.meuglossario.ui.navigation

/** Rotas e nomes de argumentos da navegação. */
object Destinos {
    const val ARG_GLOSSARIO_ID = "glossarioId"
    const val ARG_VERBETE_ID = "verbeteId"

    const val HOME = "home"
    const val ESTATISTICAS = "estatisticas"
    const val TAGS = "tags"
    const val BACKUP = "backup"

    const val VERBETES = "verbetes/{$ARG_GLOSSARIO_ID}"
    fun verbetes(glossarioId: Long) = "verbetes/$glossarioId"

    const val DETALHE_VERBETE = "verbete/{$ARG_GLOSSARIO_ID}/{$ARG_VERBETE_ID}"
    fun detalheVerbete(glossarioId: Long, verbeteId: Long) = "verbete/$glossarioId/$verbeteId"

    const val EDITAR_VERBETE = "editar/{$ARG_GLOSSARIO_ID}/{$ARG_VERBETE_ID}"
    fun editarVerbete(glossarioId: Long, verbeteId: Long = 0) = "editar/$glossarioId/$verbeteId"
}
