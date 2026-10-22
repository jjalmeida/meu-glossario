package com.meuglossario

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.meuglossario.di.initKoin
import com.meuglossario.ui.App
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    // A fonte com suporte a japonês (Noto Sans JP) é aplicada via Typography no
    // tema e carregada automaticamente pelo Compose Resources no primeiro uso.
    // O index.html faz <link rel="preload"> da fonte para reduzir o flash inicial.
    ComposeViewport(document.body!!) {
        App()
    }
}
