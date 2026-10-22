package com.meuglossario.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VerbeteEditScreen(
    glossarioId: Long,
    verbeteId: Long,
    onConcluir: () -> Unit,
    viewModel: VerbeteEditViewModel = koinViewModel { parametersOf(glossarioId, verbeteId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val sugestoes by viewModel.tagsSugeridas.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var novaTag by remember { mutableStateOf("") }

    LaunchedEffect(uiState.salvo) { if (uiState.salvo) onConcluir() }
    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let {
            snackbarHostState.showSnackbar(it.texto)
            viewModel.mensagemConsumida()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.ehEdicao) "Editar verbete" else "Novo verbete") },
                navigationIcon = {
                    IconButton(onClick = onConcluir) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Cancelar") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.palavra,
                onValueChange = viewModel::onPalavraChange,
                label = { Text("Palavra *") },
                singleLine = true,
                isError = uiState.palavraErro != null,
                supportingText = { uiState.palavraErro?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.definicao,
                onValueChange = viewModel::onDefinicaoChange,
                label = { Text("Definição *") },
                isError = uiState.definicaoErro != null,
                supportingText = { uiState.definicaoErro?.let { Text(it) } },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.observacao,
                onValueChange = viewModel::onObservacaoChange,
                label = { Text("Observação") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.referencia,
                onValueChange = viewModel::onReferenciaChange,
                label = { Text("Referência") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tags", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = novaTag,
                    onValueChange = { novaTag = it },
                    label = { Text("Nova tag") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = { viewModel.adicionarTag(novaTag); novaTag = "" }) {
                    Icon(Icons.Default.Add, "Adicionar tag")
                }
            }

            if (uiState.tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    uiState.tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { viewModel.removerTag(tag) },
                            label = { Text(tag) },
                            trailingIcon = { Icon(Icons.Default.Close, "Remover") }
                        )
                    }
                }
            }

            if (sugestoes.isNotEmpty()) {
                Text("Sugestões", style = MaterialTheme.typography.labelSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    sugestoes.filter { s -> uiState.tags.none { it.equals(s, ignoreCase = true) } }
                        .forEach { s ->
                            InputChip(selected = false, onClick = { viewModel.adicionarTag(s) }, label = { Text(s) })
                        }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Favorito", modifier = Modifier.weight(1f))
                Switch(checked = uiState.favorito, onCheckedChange = viewModel::onFavoritoChange)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onConcluir, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(onClick = viewModel::salvar, modifier = Modifier.weight(1f)) { Text("Salvar") }
            }
        }
    }
}
