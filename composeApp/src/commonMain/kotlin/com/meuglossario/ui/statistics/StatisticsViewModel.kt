package com.meuglossario.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meuglossario.domain.model.Estatisticas
import com.meuglossario.domain.usecase.ObterEstatisticasUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    obterEstatisticasUseCase: ObterEstatisticasUseCase
) : ViewModel() {
    val estatisticas: StateFlow<Estatisticas> = obterEstatisticasUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Estatisticas())
}
