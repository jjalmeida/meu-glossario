package com.meuglossario.platform

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import com.google.api.services.drive.model.File as DriveFile

/**
 * Backup no Drive para Desktop via fluxo OAuth de "app instalado".
 * Requer um arquivo de credenciais `client_secret.json` (tipo Desktop) em
 * ~/.meuglossario/. Tokens são guardados em ~/.meuglossario/tokens.
 */
class DesktopBackupService : BackupService {

    private val baseDir = File(System.getProperty("user.home"), ".meuglossario").apply { mkdirs() }
    private val secretsFile = File(baseDir, "client_secret.json")
    private val tokensDir = File(baseDir, "tokens").apply { mkdirs() }

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val transport by lazy { GoogleNetHttpTransport.newTrustedTransport() }

    @Volatile private var credential: Credential? = null

    override fun estaConectado(): Boolean = credential != null

    override fun contaConectada(): String? = if (credential != null) "conta Google conectada" else null

    override suspend fun conectar(): Boolean = withContext(Dispatchers.IO) {
        try {
            autorizar()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun autorizar(): Credential {
        credential?.let { return it }
        require(secretsFile.exists()) {
            "Credenciais ausentes: coloque client_secret.json em ${baseDir.absolutePath}."
        }
        val secrets = secretsFile.inputStream().use {
            GoogleClientSecrets.load(jsonFactory, it.reader())
        }
        val flow = GoogleAuthorizationCodeFlow.Builder(
            transport, jsonFactory, secrets, listOf(DriveScopes.DRIVE_APPDATA)
        ).setDataStoreFactory(FileDataStoreFactory(tokensDir))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        val cred = AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
        credential = cred
        return cred
    }

    private fun drive(): Drive = Drive.Builder(transport, jsonFactory, autorizar())
        .setApplicationName("Meu Glossário").build()

    override suspend fun enviarBackup(conteudo: String) = withContext(Dispatchers.IO) {
        val drive = drive()
        val existente = localizar(drive)
        val media = ByteArrayContent("application/json", conteudo.toByteArray())
        if (existente == null) {
            val metadata = DriveFile().apply {
                name = BACKUP_FILE
                parents = listOf("appDataFolder")
            }
            drive.files().create(metadata, media).setFields("id").execute()
        } else {
            drive.files().update(existente, DriveFile(), media).execute()
        }
        Unit
    }

    override suspend fun baixarBackup(): String? = withContext(Dispatchers.IO) {
        val drive = drive()
        val id = localizar(drive) ?: return@withContext null
        val out = ByteArrayOutputStream()
        drive.files().get(id).executeMediaAndDownloadTo(out)
        out.toString("UTF-8")
    }

    private fun localizar(drive: Drive): String? =
        drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILE'")
            .setFields("files(id, name)")
            .execute()
            .files?.firstOrNull()?.id

    companion object {
        private const val BACKUP_FILE = "meu_glossario_backup.json"
    }
}
