package com.meuglossario.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meuglossario.domain.ValidacaoException
import com.meuglossario.domain.repository.TagRepository
import com.meuglossario.ui.common.UiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TagItemUi(val id: Long, val nome: String, val total: Int)

class TagsViewModel(
    private val tagRepository: TagRepository
) : ViewModel() {

    val tags: StateFlow<List<TagItemUi>> = combine(
        tagRepository.observarTags(),
        tagRepository.observarContagemPorTag()
    ) { lista, contagens ->
        val mapa = contagens.associateBy({ it.nome.lowercase() }, { it.total })
        lista.map { TagItemUi(it.id, it.nome, mapa[it.nome.lowercase()] ?: 0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mensagem = MutableStateFlow<UiMessage?>(null)
    val mensagem: StateFlow<UiMessage?> = _mensagem.asStateFlow()

    fun criarTag(nome: String) = executar { tagRepository.criar(nome); "Tag criada." }
    fun renomearTag(id: Long, novoNome: String) = executar { tagRepository.renomear(id, novoNome); "Tag atualizada." }
    fun excluirTag(id: Long) = executar { tagRepository.excluir(id); "Tag excluída." }

    private fun executar(bloco: suspend () -> String) {
        viewModelScope.launch {
            try {
                _mensagem.value = UiMessage(bloco())
            } catch (e: ValidacaoException) {
                _mensagem.value = UiMessage(e.message ?: "Erro.")
            }
        }
    }

    fun mensagemConsumida() { _mensagem.value = null }
}
