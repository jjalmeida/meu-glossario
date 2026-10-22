package com.meuglossario.ui.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.meuglossario.ui.components.ConfirmarDialog
import com.meuglossario.ui.components.EditarNomeDialog
import com.meuglossario.ui.components.EstadoVazio
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    onVoltar: () -> Unit,
    viewModel: TagsViewModel = koinViewModel()
) {
    val tags by viewModel.tags.collectAsState()
    val mensagem by viewModel.mensagem.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var novaTag by remember { mutableStateOf("") }
    var emEdicao by remember { mutableStateOf<TagItemUi?>(null) }
    var paraExcluir by remember { mutableStateOf<TagItemUi?>(null) }

    LaunchedEffect(mensagem) {
        mensagem?.let { snackbarHostState.showSnackbar(it.texto); viewModel.mensagemConsumida() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tags / Categorias") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
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
                OutlinedButton(onClick = { viewModel.criarTag(novaTag); novaTag = "" }) {
                    Icon(Icons.Default.Add, "Criar")
                }
            }

            if (tags.isEmpty()) {
                EstadoVazio("Nenhuma tag cadastrada.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tags, key = { it.id }) { tag ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tag.nome, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${tag.total} verbete(s)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { emEdicao = tag }) { Icon(Icons.Default.Edit, "Editar") }
                                IconButton(onClick = { paraExcluir = tag }) { Icon(Icons.Default.Delete, "Excluir") }
                            }
                        }
                    }
                }
            }
        }
    }

    emEdicao?.let { tag ->
        EditarNomeDialog(
            nomeAtual = tag.nome,
            titulo = "Editar tag",
            onConfirmar = { viewModel.renomearTag(tag.id, it); emEdicao = null },
            onDismiss = { emEdicao = null }
        )
    }
    paraExcluir?.let { tag ->
        ConfirmarDialog(
            titulo = "Excluir tag",
            mensagem = "Excluir a tag \"${tag.nome}\"?",
            onConfirmar = { viewModel.excluirTag(tag.id); paraExcluir = null },
            onDismiss = { paraExcluir = null }
        )
    }
}
