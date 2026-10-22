package com.meuglossario.platform

import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * Backup no Google Drive na web. A autenticação OAuth (Google Identity
 * Services) e as chamadas à Drive REST API são feitas pelo bridge JavaScript
 * `driveBridge.js` (carregado pelo index.html), exposto em window.MeuGlossarioDrive.
 *
 * Configure o OAuth Client ID (tipo "Web") em `driveBridge.js`.
 */

private fun jsDriveDisponivel(): Boolean =
    js("(typeof window.MeuGlossarioDrive !== 'undefined')")

private fun jsDriveConectado(): Boolean =
    js("(window.MeuGlossarioDrive ? window.MeuGlossarioDrive.isConnected() : false)")

private fun jsDriveConta(): String? =
    js("(window.MeuGlossarioDrive ? window.MeuGlossarioDrive.account() : null)")

private fun jsDriveSignIn(): Promise<JsAny?> =
    js("window.MeuGlossarioDrive.signIn()")

private fun jsDriveUpload(conteudo: String): Promise<JsAny?> =
    js("window.MeuGlossarioDrive.upload(conteudo)")

private fun jsDriveDownload(): Promise<JsString?> =
    js("window.MeuGlossarioDrive.download()")

class WebBackupService : BackupService {

    override fun estaConectado(): Boolean = jsDriveDisponivel() && jsDriveConectado()

    override fun contaConectada(): String? = if (jsDriveDisponivel()) jsDriveConta() else null

    override suspend fun conectar(): Boolean {
        if (!jsDriveDisponivel()) error("Integração com Google Drive indisponível.")
        jsDriveSignIn().await<JsAny?>()
        return jsDriveConectado()
    }

    override suspend fun enviarBackup(conteudo: String) {
        if (!jsDriveDisponivel()) error("Integração com Google Drive indisponível.")
        jsDriveUpload(conteudo).await<JsAny?>()
    }

    override suspend fun baixarBackup(): String? {
        if (!jsDriveDisponivel()) error("Integração com Google Drive indisponível.")
        return jsDriveDownload().await<JsString?>()?.toString()
    }
}
