package com.meuglossario.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun EditarNomeDialog(
    nomeAtual: String,
    titulo: String = "Editar nome",
    onConfirmar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var texto by remember { mutableStateOf(nomeAtual) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            OutlinedTextField(value = texto, onValueChange = { texto = it }, singleLine = true, label = { Text("Nome") })
        },
        confirmButton = { TextButton(onClick = { onConfirmar(texto) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun ConfirmarDialog(
    titulo: String,
    mensagem: String,
    textoConfirmar: String = "Excluir",
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = { Text(mensagem) },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
