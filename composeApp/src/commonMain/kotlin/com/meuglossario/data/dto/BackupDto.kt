package com.meuglossario.data.dto

import kotlinx.serialization.Serializable

/** DTO de backup completo (todos os glossários), usado pela integração com o Drive. */
@Serializable
data class BackupDto(
    val versao: Int = 1,
    val dataBackup: String = "",
    val glossarios: List<GlossarioDto> = emptyList()
)
