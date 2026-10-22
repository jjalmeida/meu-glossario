package com.meuglossario.platform

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Backup no Drive para Android usando a conta conectada via Google Sign-In
 * (escopo appDataFolder). O login em si é iniciado pela Activity.
 */
class AndroidBackupService(private val context: Context) : BackupService {

    override fun estaConectado(): Boolean =
        GoogleSignIn.getLastSignedInAccount(context) != null

    override fun contaConectada(): String? =
        GoogleSignIn.getLastSignedInAccount(context)?.email

    override suspend fun conectar(): Boolean =
        AndroidSignInBridge.instance?.signIn() ?: false

    private fun drive(): Drive {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: error("Nenhuma conta Google conectada.")
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        ).apply { selectedAccount = account.account }
        return Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("Meu Glossário").build()
    }

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

/** Ponte para a Activity conduzir o Google Sign-In de forma suspensa. */
object AndroidSignInBridge {
    @Volatile var instance: SignIn? = null

    interface SignIn {
        suspend fun signIn(): Boolean
    }
}
