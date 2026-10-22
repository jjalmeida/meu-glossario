package com.meuglossario.data.prefs

import com.meuglossario.data.local.KeyValueStore

/**
 * Persiste o estado de onboarding e de descoberta de novidades (feature
 * discovery), usando o KeyValueStore multiplataforma.
 */
class OnboardingPreferences(private val store: KeyValueStore) {

    /** Se o usuário já concluiu (ou pulou) o onboarding inicial. */
    var onboardingConcluido: Boolean
        get() = store.getString(KEY_ONBOARDING) == "true"
        set(value) = store.putString(KEY_ONBOARDING, value.toString())

    /**
     * Maior "versão de features" que o usuário já viu. Serve para decidir
     * quais novidades mostrar após uma atualização. 0 = nunca viu nada.
     */
    var ultimaVersaoFeaturesVista: Int
        get() = store.getString(KEY_ULTIMA_VERSAO_FEATURES)?.toIntOrNull() ?: 0
        set(value) = store.putString(KEY_ULTIMA_VERSAO_FEATURES, value.toString())

    companion object {
        private const val KEY_ONBOARDING = "onboarding_concluido"
        private const val KEY_ULTIMA_VERSAO_FEATURES = "ultima_versao_features"
    }
}
