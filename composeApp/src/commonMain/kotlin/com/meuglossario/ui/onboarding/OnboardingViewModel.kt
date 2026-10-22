package com.meuglossario.ui.onboarding

import androidx.lifecycle.ViewModel
import com.meuglossario.data.prefs.OnboardingPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IntroUiState(
    val mostrarOnboarding: Boolean = false,
    val novidades: List<Novidade> = emptyList()
)

/**
 * Decide o que exibir na inicialização:
 * - Onboarding, se o usuário nunca concluiu.
 * - Feature discovery (novidades), se já concluiu mas há features novas desde
 *   a última versão vista.
 */
class OnboardingViewModel(
    private val preferences: OnboardingPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(calcularEstadoInicial())
    val uiState: StateFlow<IntroUiState> = _uiState.asStateFlow()

    private fun calcularEstadoInicial(): IntroUiState {
        if (!preferences.onboardingConcluido) {
            return IntroUiState(mostrarOnboarding = true)
        }
        val vista = preferences.ultimaVersaoFeaturesVista
        val novas = OnboardingContent.novidades
            .filter { it.versao > vista }
            .sortedBy { it.versao }
        return IntroUiState(novidades = novas)
    }

    /** Conclui o onboarding: marca como visto e também zera as novidades pendentes. */
    fun concluirOnboarding() {
        preferences.onboardingConcluido = true
        preferences.ultimaVersaoFeaturesVista = OnboardingContent.VERSAO_FEATURES_ATUAL
        _uiState.value = IntroUiState()
    }

    /** Marca as novidades como vistas. */
    fun concluirNovidades() {
        preferences.ultimaVersaoFeaturesVista = OnboardingContent.VERSAO_FEATURES_ATUAL
        _uiState.value = _uiState.value.copy(novidades = emptyList())
    }
}
