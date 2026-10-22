package com.meuglossario.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meuglossario.domain.model.Verbete
import com.meuglossario.domain.usecase.AlternarFavoritoUseCase
import com.meuglossario.domain.usecase.ExcluirVerbeteUseCase
import com.meuglossario.domain.usecase.ObservarVerbeteUseCase
import com.meuglossario.platform.ExportService
import com.meuglossario.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VerbeteDetailViewModel(
    private val verbeteId: Long,
    observarVerbeteUseCase: ObservarVerbeteUseCase,
    private val alternarFavoritoUseCase: AlternarFavoritoUseCase,
    private val excluirVerbeteUseCase: ExcluirVerbeteUseCase,
    private val exportService: ExportService
) : ViewModel() {

    val verbete: StateFlow<Verbete?> = observarVerbeteUseCase(verbeteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _mensagem = MutableStateFlow<UiMessage?>(null)
    val mensagem: StateFlow<UiMessage?> = _mensagem

    fun alternarFavorito() {
        val atual = verbete.value ?: return
        viewModelScope.launch { alternarFavoritoUseCase(atual.id, !atual.favorito) }
    }

    fun compartilhar() {
        val atual = verbete.value ?: return
        viewModelScope.launch {
            val msg = exportService.compartilharVerbete(atual)
            _mensagem.value = UiMessage(msg)
        }
    }

    fun excluir(onExcluido: () -> Unit) {
        val atual = verbete.value ?: return
        viewModelScope.launch {
            excluirVerbeteUseCase(atual.id)
            onExcluido()
        }
    }

    fun mensagemConsumida() { _mensagem.value = null }
}
