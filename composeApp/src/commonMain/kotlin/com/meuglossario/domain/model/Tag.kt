package com.meuglossario.domain.model

import kotlinx.serialization.Serializable

/** Modelo de domínio de uma tag/categoria. */
@Serializable
data class Tag(
    val id: Long = 0,
    val nome: String
)
