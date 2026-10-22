package com.meuglossario

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.meuglossario.di.initKoin
import com.meuglossario.ui.App

fun main() {
    initKoin()
    application {
        val state = rememberWindowState(width = 1000.dp, height = 720.dp)
        Window(
            onCloseRequest = ::exitApplication,
            state = state,
            title = "Meu Glossário",
            icon = AppIconPainter()
        ) {
            App()
        }
    }
}
