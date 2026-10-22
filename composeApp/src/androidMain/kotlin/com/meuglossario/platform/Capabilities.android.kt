package com.meuglossario.platform

/** Android: suporte completo a PDF, compartilhamento nativo e Google Drive. */
class AndroidPlatformCapabilities : PlatformCapabilities {
    override val nomePlataforma = "Android"
    override val suportaPdf = true
    override val suportaCompartilhamentoNativo = true

    // Google Drive temporariamente desabilitado. O código de backup/restore
    // permanece no projeto (AndroidBackupService); para reabilitar, troque
    // este valor de volta para `true`.
    override val suportaGoogleDrive = false
}
