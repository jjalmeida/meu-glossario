package com.meuglossario.ui.verbetes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.model.Verbete
import com.meuglossario.domain.model.VerbeteFiltro
import com.meuglossario.domain.model.VerbeteOrdenacao
import com.meuglossario.domain.usecase.AlternarFavoritoUseCase
import com.meuglossario.domain.usecase.ExcluirGlossarioUseCase
import com.meuglossario.domain.usecase.ExcluirVerbeteUseCase
import com.meuglossario.domain.usecase.FiltrarVerbetesUseCase
import com.meuglossario.domain.usecase.ObservarGlossarioUseCase
import com.meuglossario.domain.usecase.ObservarVerbetesUseCase
import com.meuglossario.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VerbetesUiState(
    val query: String = "",
    val filtro: VerbeteFiltro = VerbeteFiltro.TODOS,
    val ordenacao: VerbeteOrdenacao = VerbeteOrdenacao.DATA_CRIACAO,
    val tagSelecionada: String? = null,
    val modoSelecao: Boolean = false,
    val selecionados: Set<Long> = emptySet(),
    val mensagem: UiMessage? = null
)

class VerbetesViewModel(
    val glossarioId: Long,
    observarGlossarioUseCase: ObservarGlossarioUseCase,
    observarVerbetesUseCase: ObservarVerbetesUseCase,
    private val filtrarVerbetesUseCase: FiltrarVerbetesUseCase,
    private val excluirVerbeteUseCase: ExcluirVerbeteUseCase,
    private val excluirGlossarioUseCase: ExcluirGlossarioUseCase,
    private val alternarFavoritoUseCase: AlternarFavoritoUseCase
) : ViewModel() {

    val glossario: StateFlow<Glossario?> = observarGlossarioUseCase(glossarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val verbetes: StateFlow<List<Verbete>> = observarVerbetesUseCase(glossarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(VerbetesUiState())
    val uiState: StateFlow<VerbetesUiState> = _uiState

    val verbetesFiltrados: StateFlow<List<Verbete>> =
        combine(verbetes, _uiState) { lista, state ->
            filtrarVerbetesUseCase(lista, state.query, state.filtro, state.ordenacao, state.tagSelecionada)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalVerbetes: StateFlow<Int> = verbetes
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun onQueryChange(v: String) { _uiState.value = _uiState.value.copy(query = v) }
    fun onFiltroChange(f: VerbeteFiltro) { _uiState.value = _uiState.value.copy(filtro = f) }
    fun onOrdenacaoChange(o: VerbeteOrdenacao) { _uiState.value = _uiState.value.copy(ordenacao = o) }
    fun onTagSelecionada(t: String?) { _uiState.value = _uiState.value.copy(tagSelecionada = t) }

    fun alternarFavorito(verbete: Verbete) {
        viewModelScope.launch { alternarFavoritoUseCase(verbete.id, !verbete.favorito) }
    }

    fun excluirVerbete(id: Long) {
        viewModelScope.launch {
            excluirVerbeteUseCase(id)
            _uiState.value = _uiState.value.copy(mensagem = UiMessage("Verbete excluído."))
        }
    }

    // ---- Seleção múltipla ----

    /** Entra no modo de seleção já marcando o item que iniciou (long-press). */
    fun iniciarSelecao(id: Long) {
        _uiState.value = _uiState.value.copy(modoSelecao = true, selecionados = setOf(id))
    }

    /** Marca/desmarca um item; sai do modo de seleção se esvaziar. */
    fun alternarSelecao(id: Long) {
        val atual = _uiState.value.selecionados
        val novo = if (id in atual) atual - id else atual + id
        _uiState.value = _uiState.value.copy(
            selecionados = novo,
            modoSelecao = novo.isNotEmpty()
        )
    }

    fun selecionarTodos(ids: List<Long>) {
        _uiState.value = _uiState.value.copy(modoSelecao = true, selecionados = ids.toSet())
    }

    fun limparSelecao() {
        _uiState.value = _uiState.value.copy(modoSelecao = false, selecionados = emptySet())
    }

    /** Exclui todos os verbetes atualmente selecionados. */
    fun excluirSelecionados() {
        val ids = _uiState.value.selecionados
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { excluirVerbeteUseCase(it) }
            _uiState.value = _uiState.value.copy(
                modoSelecao = false,
                selecionados = emptySet(),
                mensagem = UiMessage("${ids.size} verbete(s) excluído(s).")
            )
        }
    }

    /** Exclui o glossário inteiro (e seus verbetes). Chama [onExcluido] ao concluir. */
    fun excluirGlossario(onExcluido: () -> Unit) {
        viewModelScope.launch {
            excluirGlossarioUseCase(glossarioId)
            onExcluido()
        }
    }

    fun mensagemConsumida() { _uiState.value = _uiState.value.copy(mensagem = null) }
}
