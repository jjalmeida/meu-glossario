package com.meuglossario.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meuglossario.domain.ValidacaoException
import com.meuglossario.domain.model.Tag
import com.meuglossario.domain.model.Verbete
import com.meuglossario.domain.repository.TagRepository
import com.meuglossario.domain.repository.VerbeteRepository
import com.meuglossario.domain.usecase.SalvarVerbeteUseCase
import com.meuglossario.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VerbeteEditUiState(
    val palavra: String = "",
    val definicao: String = "",
    val observacao: String = "",
    val referencia: String = "",
    val tags: List<String> = emptyList(),
    val favorito: Boolean = false,
    val palavraErro: String? = null,
    val definicaoErro: String? = null,
    val salvo: Boolean = false,
    val carregando: Boolean = true,
    val mensagem: UiMessage? = null
)

class VerbeteEditViewModel(
    private val glossarioId: Long,
    private val verbeteId: Long,
    private val verbeteRepository: VerbeteRepository,
    private val salvarVerbeteUseCase: SalvarVerbeteUseCase,
    tagRepository: TagRepository
) : ViewModel() {

    val ehEdicao: Boolean = verbeteId != 0L

    val tagsSugeridas: StateFlow<List<String>> = tagRepository.observarTags()
        .map { list -> list.map(Tag::nome) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(VerbeteEditUiState())
    val uiState: StateFlow<VerbeteEditUiState> = _uiState.asStateFlow()

    init {
        if (ehEdicao) carregar() else _uiState.value = _uiState.value.copy(carregando = false)
    }

    private fun carregar() {
        viewModelScope.launch {
            val verbete = verbeteRepository.buscarPorId(verbeteId)
            _uiState.value = if (verbete != null) {
                VerbeteEditUiState(
                    palavra = verbete.palavra,
                    definicao = verbete.definicao,
                    observacao = verbete.observacao,
                    referencia = verbete.referencia,
                    tags = verbete.tags.map { it.nome },
                    favorito = verbete.favorito,
                    carregando = false
                )
            } else {
                _uiState.value.copy(carregando = false)
            }
        }
    }

    fun onPalavraChange(v: String) { _uiState.value = _uiState.value.copy(palavra = v, palavraErro = null) }
    fun onDefinicaoChange(v: String) { _uiState.value = _uiState.value.copy(definicao = v, definicaoErro = null) }
    fun onObservacaoChange(v: String) { _uiState.value = _uiState.value.copy(observacao = v) }
    fun onReferenciaChange(v: String) { _uiState.value = _uiState.value.copy(referencia = v) }
    fun onFavoritoChange(v: Boolean) { _uiState.value = _uiState.value.copy(favorito = v) }

    fun adicionarTag(nome: String) {
        val limpo = nome.trim()
        if (limpo.isEmpty()) return
        val atual = _uiState.value.tags
        if (atual.any { it.equals(limpo, ignoreCase = true) }) return
        _uiState.value = _uiState.value.copy(tags = atual + limpo)
    }

    fun removerTag(nome: String) {
        _uiState.value = _uiState.value.copy(tags = _uiState.value.tags.filterNot { it == nome })
    }

    fun salvar() {
        val state = _uiState.value
        var erroP: String? = null
        var erroD: String? = null
        if (state.palavra.trim().isEmpty()) erroP = "A palavra é obrigatória."
        if (state.definicao.trim().isEmpty()) erroD = "A definição é obrigatória."
        if (erroP != null || erroD != null) {
            _uiState.value = state.copy(palavraErro = erroP, definicaoErro = erroD)
            return
        }
        viewModelScope.launch {
            try {
                val verbete = Verbete(
                    id = verbeteId,
                    glossarioId = glossarioId,
                    palavra = state.palavra,
                    definicao = state.definicao,
                    observacao = state.observacao,
                    referencia = state.referencia,
                    favorito = state.favorito
                )
                salvarVerbeteUseCase(verbete, state.tags)
                _uiState.value = _uiState.value.copy(salvo = true)
            } catch (e: ValidacaoException) {
                _uiState.value = _uiState.value.copy(mensagem = UiMessage(e.message ?: "Erro ao salvar."))
            }
        }
    }

    fun mensagemConsumida() { _uiState.value = _uiState.value.copy(mensagem = null) }
}
