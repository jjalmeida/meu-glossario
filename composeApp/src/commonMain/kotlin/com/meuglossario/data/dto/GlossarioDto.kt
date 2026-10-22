package com.meuglossario.data.dto

import kotlinx.serialization.Serializable

/**
 * DTO usado para exportar/importar um glossário completo em JSON,
 * serializado com kotlinx.serialization.
 */
@Serializable
data class GlossarioDto(
    val nome: String = "",
    val dataCriacao: String = "",
    val verbetes: List<VerbeteDto> = emptyList()
)

@Serializable
data class VerbeteDto(
    val palavra: String = "",
    val definicao: String = "",
    val observacao: String = "",
    val referencia: String = "",
    val favorito: Boolean = false,
    val tags: List<String> = emptyList()
)
