package com.meuglossario.platform

/**
 * Integração de backup no Google Drive, dependente de plataforma.
 * O conteúdo do backup é o JSON gerado por GerarBackupUseCase.
 */
interface BackupService {
    /** Se há uma conta/sessão Google conectada. */
    fun estaConectado(): Boolean

    /** E-mail/identificação da conta conectada, se houver. */
    fun contaConectada(): String?

    /** Inicia o fluxo de login/autorização. Retorna true se conectado. */
    suspend fun conectar(): Boolean

    /** Envia (cria ou atualiza) o backup no Drive. */
    suspend fun enviarBackup(conteudo: String)

    /** Baixa o backup mais recente, ou null se não existir. */
    suspend fun baixarBackup(): String?
}
