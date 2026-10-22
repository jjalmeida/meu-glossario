package com.meuglossario.platform

/** No navegador: sem PDF nativo e sem share nativo; Drive via OAuth web. */
class WebPlatformCapabilities : PlatformCapabilities {
    override val nomePlataforma = "Web"
    override val suportaPdf = false
    override val suportaCompartilhamentoNativo = false

    // Google Drive temporariamente desabilitado. O código de backup/restore
    // permanece no projeto (WebBackupService + driveBridge.js); para
    // reabilitar, troque este valor de volta para `true`.
    override val suportaGoogleDrive = false
}
