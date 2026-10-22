package com.meuglossario.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meuglossario.domain.ValidacaoException
import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.usecase.CriarGlossarioUseCase
import com.meuglossario.domain.usecase.ExcluirGlossarioUseCase
import com.meuglossario.domain.usecase.ExportarJsonUseCase
import com.meuglossario.domain.usecase.ObservarGlossariosUseCase
import com.meuglossario.domain.usecase.RenomearGlossarioUseCase
import com.meuglossario.domain.repository.VerbeteRepository
import com.meuglossario.platform.ExportService
import com.meuglossario.platform.PlatformCapabilities
import com.meuglossario.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val novoNome: String = "",
    val mensagem: UiMessage? = null
)

class HomeViewModel(
    observarGlossariosUseCase: ObservarGlossariosUseCase,
    private val criarGlossarioUseCase: CriarGlossarioUseCase,
    private val renomearGlossarioUseCase: RenomearGlossarioUseCase,
    private val excluirGlossarioUseCase: ExcluirGlossarioUseCase,
    private val exportarJsonUseCase: ExportarJsonUseCase,
    private val verbeteRepository: VerbeteRepository,
    private val exportService: ExportService,
    val capabilities: PlatformCapabilities
) : ViewModel() {

    val glossarios: StateFlow<List<Glossario>> = observarGlossariosUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onNovoNomeChange(valor: String) {
        _uiState.value = _uiState.value.copy(novoNome = valor)
    }

    fun criarGlossario() {
        val nome = _uiState.value.novoNome
        viewModelScope.launch {
            try {
                criarGlossarioUseCase(nome)
                _uiState.value = _uiState.value.copy(novoNome = "", mensagem = UiMessage("Glossário criado."))
            } catch (e: ValidacaoException) {
                mostrar(e.message ?: "Não foi possível criar o glossário.")
            }
        }
    }

    fun renomearGlossario(id: Long, novoNome: String) {
        viewModelScope.launch {
            try {
                renomearGlossarioUseCase(id, novoNome)
                mostrar("Glossário atualizado.")
            } catch (e: ValidacaoException) {
                mostrar(e.message ?: "Não foi possível renomear.")
            }
        }
    }

    fun excluirGlossario(id: Long) {
        viewModelScope.launch {
            excluirGlossarioUseCase(id)
            mostrar("Glossário excluído.")
        }
    }

    fun exportarJson(glossario: Glossario, compartilhar: Boolean) {
        viewModelScope.launch {
            try {
                val json = exportarJsonUseCase(glossario.id)
                val msg = exportService.salvarTexto("${glossario.nome}.json", json, "application/json")
                mostrar(msg)
            } catch (e: Exception) {
                mostrar(e.message ?: "Falha ao exportar JSON.")
            }
        }
    }

    fun exportarPdf(glossario: Glossario) {
        viewModelScope.launch {
            try {
                val verbetes = verbeteRepository.listar(glossario.id)
                val msg = exportService.exportarPdf(glossario, verbetes)
                mostrar(msg)
            } catch (e: Exception) {
                mostrar(e.message ?: "Falha ao exportar PDF.")
            }
        }
    }

    private fun mostrar(texto: String) {
        _uiState.value = _uiState.value.copy(mensagem = UiMessage(texto))
    }

    fun mensagemConsumida() {
        _uiState.value = _uiState.value.copy(mensagem = null)
    }
}
