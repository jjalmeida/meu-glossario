package com.meuglossario.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Feature discovery: diálogo que apresenta as novidades desde a última versão
 * vista pelo usuário (exibido após uma atualização do app).
 */
@Composable
fun NovidadesDialog(
    novidades: List<Novidade>,
    onFechar: () -> Unit
) {
    if (novidades.isEmpty()) return

    AlertDialog(
        onDismissRequest = onFechar,
        icon = { Icon(Icons.Default.Celebration, contentDescription = null) },
        title = { Text("Novidades") },
        text = {
            Column(modifier = Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                Text(
                    "Veja o que há de novo nesta versão:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                novidades.forEach { n ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Icon(
                            imageVector = n.icone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp).padding(end = 4.dp)
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(n.titulo, style = MaterialTheme.typography.titleSmall)
                            Text(
                                n.descricao,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onFechar) { Text("Entendi") }
        }
    )
}
