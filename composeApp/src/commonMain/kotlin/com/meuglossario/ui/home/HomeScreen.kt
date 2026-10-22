package com.meuglossario.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meuglossario.domain.model.Glossario
import com.meuglossario.ui.components.ConfirmarDialog
import com.meuglossario.ui.components.EditarNomeDialog
import com.meuglossario.ui.components.EstadoVazio
import com.meuglossario.ui.components.GlossarioCard
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGlossarioClick: (Long) -> Unit,
    onEstatisticasClick: () -> Unit,
    onTagsClick: () -> Unit,
    onBackupClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val glossarios by viewModel.glossarios.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var acao by remember { mutableStateOf<Glossario?>(null) }
    var emEdicao by remember { mutableStateOf<Glossario?>(null) }
    var paraExcluir by remember { mutableStateOf<Glossario?>(null) }

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
                title = { Text("Meu Glossário") },
                actions = {
                    IconButton(onClick = onTagsClick) { Icon(Icons.Default.Label, "Tags") }
                    IconButton(onClick = onEstatisticasClick) { Icon(Icons.Default.BarChart, "Estatísticas") }
                    IconButton(onClick = onBackupClick) { Icon(Icons.Default.CloudUpload, "Backup") }
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
                    value = uiState.novoNome,
                    onValueChange = viewModel::onNovoNomeChange,
                    label = { Text("Nome do novo glossário") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = viewModel::criarGlossario) {
                    Icon(Icons.Default.Add, null)
                    Text("Criar")
                }
            }

            if (glossarios.isEmpty()) {
                EstadoVazio("Nenhum glossário ainda. Crie o primeiro acima.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(glossarios, key = { it.id }) { g ->
                        GlossarioCard(g, onClick = { onGlossarioClick(g.id) }, onLongClick = { acao = g })
                    }
                }
            }
        }
    }

    acao?.let { g ->
        ModalBottomSheet(onDismissRequest = { acao = null }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Text(g.nome, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                ListItem(
                    headlineContent = { Text("Editar nome") },
                    leadingContent = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.clickable { emEdicao = g; acao = null }
                )
                if (viewModel.capabilities.suportaPdf) {
                    ListItem(
                        headlineContent = { Text("Exportar PDF") },
                        leadingContent = { Icon(Icons.Default.PictureAsPdf, null) },
                        modifier = Modifier.clickable { viewModel.exportarPdf(g); acao = null }
                    )
                }
                ListItem(
                    headlineContent = { Text("Exportar JSON") },
                    leadingContent = { Icon(Icons.Default.DataObject, null) },
                    modifier = Modifier.clickable { viewModel.exportarJson(g, compartilhar = true); acao = null }
                )
                ListItem(
                    headlineContent = { Text("Excluir") },
                    leadingContent = { Icon(Icons.Default.Delete, null) },
                    modifier = Modifier.clickable { paraExcluir = g; acao = null }
                )
            }
        }
    }

    emEdicao?.let { g ->
        EditarNomeDialog(
            nomeAtual = g.nome,
            onConfirmar = { viewModel.renomearGlossario(g.id, it); emEdicao = null },
            onDismiss = { emEdicao = null }
        )
    }

    paraExcluir?.let { g ->
        ConfirmarDialog(
            titulo = "Excluir glossário",
            mensagem = "Excluir \"${g.nome}\" e todos os seus verbetes?",
            onConfirmar = { viewModel.excluirGlossario(g.id); paraExcluir = null },
            onDismiss = { paraExcluir = null }
        )
    }
}
