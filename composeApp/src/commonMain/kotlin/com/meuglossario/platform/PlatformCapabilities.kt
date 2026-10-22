package com.meuglossario.platform

/**
 * Descreve quais recursos estão disponíveis na plataforma atual, permitindo
 * que a UI habilite/desabilite ações (ex.: PDF e compartilhamento nativo não
 * existem no navegador).
 */
interface PlatformCapabilities {
    val nomePlataforma: String
    val suportaPdf: Boolean
    val suportaCompartilhamentoNativo: Boolean
    val suportaGoogleDrive: Boolean
}
