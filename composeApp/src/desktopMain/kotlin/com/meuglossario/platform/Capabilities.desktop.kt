package com.meuglossario.platform

/** Desktop: sem PDF/compartilhamento nativo; Drive via OAuth de app instalado. */
class DesktopPlatformCapabilities : PlatformCapabilities {
    override val nomePlataforma = "Desktop"
    override val suportaPdf = false
    override val suportaCompartilhamentoNativo = false

    // Google Drive temporariamente desabilitado. O código de backup/restore
    // permanece no projeto (DesktopBackupService); para reabilitar, troque
    // este valor de volta para `true`.
    override val suportaGoogleDrive = false
}
