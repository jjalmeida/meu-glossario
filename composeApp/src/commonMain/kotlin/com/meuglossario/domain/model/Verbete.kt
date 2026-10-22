package com.meuglossario.domain.model

import kotlinx.serialization.Serializable

/** Modelo de domínio de um verbete, já com suas tags resolvidas. */
@Serializable
data class Verbete(
    val id: Long = 0,
    val glossarioId: Long,
    val palavra: String,
    val definicao: String,
    val observacao: String = "",
    val referencia: String = "",
    val favorito: Boolean = false,
    val dataCriacao: Long = 0,
    val tags: List<Tag> = emptyList()
)
