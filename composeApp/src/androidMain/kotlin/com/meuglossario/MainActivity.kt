package com.meuglossario

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.meuglossario.platform.AndroidFileBridge
import com.meuglossario.platform.AndroidSignInBridge
import com.meuglossario.ui.App
import kotlinx.coroutines.CompletableDeferred

class MainActivity : ComponentActivity(), AndroidFileBridge.FilePicker, AndroidSignInBridge.SignIn {

    private var pendingFile: CompletableDeferred<Uri?>? = null
    private var pendingSignIn: CompletableDeferred<Boolean>? = null

    private val openDocument = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> pendingFile?.complete(uri) }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            // Força a leitura do resultado: se houver falha, lança ApiException
            // com o código real (10 = DEVELOPER_ERROR / SHA-1 não registrado).
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            pendingSignIn?.complete(account != null)
        } catch (e: ApiException) {
            Log.e("MeuGlossarioSignIn", "Falha no Google Sign-In: código ${e.statusCode}", e)
            Toast.makeText(
                this,
                "Falha ao conectar (código ${e.statusCode}). " +
                    "Verifique o SHA-1/registro no Google Cloud Console.",
                Toast.LENGTH_LONG
            ).show()
            pendingSignIn?.complete(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidFileBridge.instance = this
        AndroidSignInBridge.instance = this
        enableEdgeToEdge()
        setContent { App() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (AndroidFileBridge.instance === this) AndroidFileBridge.instance = null
        if (AndroidSignInBridge.instance === this) AndroidSignInBridge.instance = null
    }

    override suspend fun pickFile(): Uri? {
        val deferred = CompletableDeferred<Uri?>()
        pendingFile = deferred
        openDocument.launch(arrayOf("application/json", "text/plain", "*/*"))
        return deferred.await()
    }

    override suspend fun signIn(): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        pendingSignIn = deferred
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        val client = GoogleSignIn.getClient(this, options)
        signInLauncher.launch(client.signInIntent)
        return deferred.await()
    }
}
