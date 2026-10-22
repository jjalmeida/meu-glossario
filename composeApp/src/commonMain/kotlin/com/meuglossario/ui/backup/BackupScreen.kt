package com.meuglossario.ui.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meuglossario.domain.model.Glossario
import com.meuglossario.util.DateUtils
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onVoltar: () -> Unit,
    viewModel: BackupViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let { snackbarHostState.showSnackbar(it.texto); viewModel.mensagemConsumida() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Backup e Importação") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.suportaDrive) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Google Drive", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (uiState.conectado) "Conectado: ${uiState.contaEmail ?: ""}" else "Nenhuma conta conectada.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.conectado) {
                            Text("Você pode fazer backup e restaurar.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Button(onClick = viewModel::conectar, enabled = !uiState.processando) {
                                Text("Conectar ao Google Drive")
                            }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Backup", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Último backup: " + (uiState.ultimoBackup?.let { DateUtils.formatarDataHora(it) } ?: "nunca"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Backup automático", modifier = Modifier.weight(1f))
                            Switch(checked = uiState.autoBackup, onCheckedChange = viewModel::definirAutoBackup)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = viewModel::realizarBackup,
                                enabled = uiState.conectado && !uiState.processando,
                                modifier = Modifier.weight(1f)
                            ) { Text("Fazer backup") }
                            OutlinedButton(
                                onClick = viewModel::restaurarBackup,
                                enabled = uiState.conectado && !uiState.processando,
                                modifier = Modifier.weight(1f)
                            ) { Text("Restaurar") }
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Importar JSON", style = MaterialTheme.typography.titleMedium)
                    Text("Selecione um arquivo .json de glossário para importar.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = viewModel::importarArquivo, enabled = !uiState.processando) {
                        Text("Selecionar arquivo")
                    }
                }
            }
        }
    }

    uiState.preview?.let { preview ->
        val glossarios by viewModel.glossarios.collectAsState()
        ImportacaoDialog(
            nomeGlossario = preview.dto.nome,
            totalVerbetes = preview.totalVerbetes,
            glossariosExistentes = glossarios,
            onCriarNovo = { viewModel.importarComoNovo() },
            onMesclar = { id -> viewModel.importarMesclando(id) },
            onDismiss = viewModel::cancelarImportacao
        )
    }
}

/**
 * Diálogo de importação: o usuário escolhe entre criar um novo glossário ou
 * mesclar o conteúdo do arquivo em um glossário já existente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportacaoDialog(
    nomeGlossario: String,
    totalVerbetes: Int,
    glossariosExistentes: List<Glossario>,
    onCriarNovo: () -> Unit,
    onMesclar: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var modoMesclar by remember { mutableStateOf(false) }
    var glossarioSelecionado by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (modoMesclar) "Mesclar com glossário" else "Importar glossário") },
        text = {
            Column {
                Text("Arquivo: \"$nomeGlossario\"")
                Text("$totalVerbetes verbete(s)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))

                if (!modoMesclar) {
                    Text("Como deseja importar?", style = MaterialTheme.typography.titleSmall)
                } else if (glossariosExistentes.isEmpty()) {
                    Text(
                        "Não há glossários para mesclar. Crie um novo.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("Escolha o glossário de destino:", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(glossariosExistentes, key = { it.id }) { g ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = glossarioSelecionado == g.id,
                                        onClick = { glossarioSelecionado = g.id }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = glossarioSelecionado == g.id,
                                    onClick = { glossarioSelecionado = g.id }
                                )
                                Text(
                                    "${g.nome} (${g.totalVerbetes})",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!modoMesclar) {
                TextButton(onClick = onCriarNovo) { Text("Criar novo") }
            } else {
                TextButton(
                    onClick = { glossarioSelecionado?.let(onMesclar) },
                    enabled = glossarioSelecionado != null
                ) { Text("Mesclar") }
            }
        },
        dismissButton = {
            if (!modoMesclar) {
                TextButton(
                    onClick = { modoMesclar = true },
                    enabled = glossariosExistentes.isNotEmpty()
                ) { Text("Mesclar com existente") }
            } else {
                TextButton(onClick = { modoMesclar = false }) { Text("Voltar") }
            }
        }
    )
}
