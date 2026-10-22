package com.meuglossario.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meuglossario.data.prefs.BackupPreferences
import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.usecase.GerarBackupUseCase
import com.meuglossario.domain.usecase.ImportarJsonUseCase
import com.meuglossario.domain.usecase.ObservarGlossariosUseCase
import com.meuglossario.domain.usecase.PreviewImportacao
import com.meuglossario.domain.usecase.RestaurarBackupUseCase
import com.meuglossario.data.local.currentTimeMillis
import com.meuglossario.platform.BackupService
import com.meuglossario.platform.ExportService
import com.meuglossario.platform.PlatformCapabilities
import com.meuglossario.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BackupUiState(
    val conectado: Boolean = false,
    val contaEmail: String? = null,
    val autoBackup: Boolean = false,
    val ultimoBackup: Long? = null,
    val processando: Boolean = false,
    val suportaDrive: Boolean = true,
    val preview: PreviewImportacao? = null,
    val mensagem: UiMessage? = null
)

class BackupViewModel(
    private val backupService: BackupService,
    private val exportService: ExportService,
    private val gerarBackupUseCase: GerarBackupUseCase,
    private val restaurarBackupUseCase: RestaurarBackupUseCase,
    private val importarJsonUseCase: ImportarJsonUseCase,
    observarGlossariosUseCase: ObservarGlossariosUseCase,
    private val preferences: BackupPreferences,
    private val capabilities: PlatformCapabilities
) : ViewModel() {

    /** Glossários existentes, para o usuário escolher ao mesclar. */
    val glossarios: StateFlow<List<Glossario>> = observarGlossariosUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(
        BackupUiState(
            conectado = backupService.estaConectado(),
            contaEmail = backupService.contaConectada(),
            autoBackup = preferences.autoBackup,
            ultimoBackup = preferences.ultimoBackup,
            suportaDrive = capabilities.suportaGoogleDrive
        )
    )
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun atualizarConta() {
        _uiState.value = _uiState.value.copy(
            conectado = backupService.estaConectado(),
            contaEmail = backupService.contaConectada()
        )
    }

    fun conectar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processando = true)
            try {
                backupService.conectar()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(mensagem = UiMessage(e.message ?: "Falha ao conectar."))
            } finally {
                _uiState.value = _uiState.value.copy(processando = false)
                atualizarConta()
            }
        }
    }

    fun realizarBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processando = true)
            try {
                val json = gerarBackupUseCase()
                backupService.enviarBackup(json)
                preferences.ultimoBackup = currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    ultimoBackup = preferences.ultimoBackup,
                    mensagem = UiMessage("Backup concluído.")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(mensagem = UiMessage(e.message ?: "Falha no backup."))
            } finally {
                _uiState.value = _uiState.value.copy(processando = false)
            }
        }
    }

    fun restaurarBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processando = true)
            try {
                val json = backupService.baixarBackup()
                    ?: throw IllegalStateException("Nenhum backup encontrado no Drive.")
                val total = restaurarBackupUseCase(json)
                _uiState.value = _uiState.value.copy(mensagem = UiMessage("$total glossário(s) restaurado(s)."))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(mensagem = UiMessage(e.message ?: "Falha ao restaurar."))
            } finally {
                _uiState.value = _uiState.value.copy(processando = false)
            }
        }
    }

    fun definirAutoBackup(habilitado: Boolean) {
        preferences.autoBackup = habilitado
        _uiState.value = _uiState.value.copy(autoBackup = habilitado)
    }

    fun importarArquivo() {
        viewModelScope.launch {
            try {
                val conteudo = exportService.selecionarArquivoTexto() ?: return@launch
                val preview = importarJsonUseCase.preVisualizar(conteudo)
                _uiState.value = _uiState.value.copy(preview = preview)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(mensagem = UiMessage(e.message ?: "Arquivo inválido."))
            }
        }
    }

    /** Importa criando um novo glossário. */
    fun importarComoNovo() {
        val preview = _uiState.value.preview ?: return
        executarImportacao { importarJsonUseCase.importarComoNovo(preview.dto) }
    }

    /** Mescla o arquivo no glossário existente escolhido pelo usuário. */
    fun importarMesclando(glossarioIdDestino: Long) {
        val preview = _uiState.value.preview ?: return
        executarImportacao { importarJsonUseCase.importarMesclando(preview.dto, glossarioIdDestino) }
    }

    private fun executarImportacao(bloco: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                bloco()
                _uiState.value = _uiState.value.copy(preview = null, mensagem = UiMessage("Importação concluída."))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(preview = null, mensagem = UiMessage(e.message ?: "Falha ao importar."))
            }
        }
    }

    fun cancelarImportacao() { _uiState.value = _uiState.value.copy(preview = null) }
    fun mensagemConsumida() { _uiState.value = _uiState.value.copy(mensagem = null) }
}
