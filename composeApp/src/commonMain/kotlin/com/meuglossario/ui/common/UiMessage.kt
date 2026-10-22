package com.meuglossario.ui.common

import com.meuglossario.data.local.currentTimeMillis

/** Mensagem única para exibir em Snackbar, consumida uma vez. */
data class UiMessage(val texto: String, val id: Long = currentTimeMillis())
