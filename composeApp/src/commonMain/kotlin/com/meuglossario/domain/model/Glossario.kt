package com.meuglossario.domain.model

import kotlinx.serialization.Serializable

/**
 * Modelo de domínio de um glossário.
 * Datas são armazenadas como epoch millis para funcionar em todas as plataformas.
 */
@Serializable
data class Glossario(
    val id: Long = 0,
    val nome: String,
    val dataCriacao: Long = 0,
    val totalVerbetes: Int = 0
)
