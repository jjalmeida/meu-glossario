package com.meuglossario.ui.verbetes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meuglossario.domain.model.Verbete
import com.meuglossario.domain.model.VerbeteFiltro
import com.meuglossario.domain.model.VerbeteOrdenacao
import com.meuglossario.ui.components.CampoBusca
import com.meuglossario.ui.components.ChipsFiltro
import com.meuglossario.ui.components.ConfirmarDialog
import com.meuglossario.ui.components.EstadoVazio
import com.meuglossario.ui.components.VerbeteCard
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbetesScreen(
    glossarioId: Long,
    onVoltar: () -> Unit,
    onVerbeteClick: (Long, Long) -> Unit,
    onNovoVerbete: (Long) -> Unit,
    onEditarVerbete: (Long, Long) -> Unit,
    viewModel: VerbetesViewModel = koinViewModel { parametersOf(glossarioId) }
) {
    val glossario by viewModel.glossario.collectAsState()
    val verbetes by viewModel.verbetesFiltrados.collectAsState()
    val total by viewModel.totalVerbetes.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var menuOrdenacao by remember { mutableStateOf(false) }
    var menuOpcoes by remember { mutableStateOf(false) }
    var confirmarExcluirGlossario by remember { mutableStateOf(false) }
    var verbeteParaExcluir by remember { mutableStateOf<Verbete?>(null) }
    var confirmarExcluirSelecionados by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let {
            snackbarHostState.showSnackbar(it.texto)
            viewModel.mensagemConsumida()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (uiState.modoSelecao) {
                // Barra de seleção múltipla
                TopAppBar(
                    title = { Text("${uiState.selecionados.size} selecionado(s)") },
                    navigationIcon = {
                        IconButton(onClick = viewModel::limparSelecao) {
                            Icon(Icons.Default.Close, "Cancelar seleção")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selecionarTodos(verbetes.map { it.id }) }) {
                            Icon(Icons.Default.SelectAll, "Selecionar todos")
                        }
                        IconButton(
                            onClick = { confirmarExcluirSelecionados = true },
                            enabled = uiState.selecionados.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Delete, "Excluir selecionados")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(glossario?.nome ?: "Verbetes")
                            Text("$total verbete(s)", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                    },
                    actions = {
                        IconButton(onClick = { menuOrdenacao = true }) { Icon(Icons.Default.Sort, "Ordenar") }
                        DropdownMenu(expanded = menuOrdenacao, onDismissRequest = { menuOrdenacao = false }) {
                            VerbeteOrdenacao.entries.forEach { o ->
                                DropdownMenuItem(text = { Text(o.rotulo) }, onClick = {
                                    viewModel.onOrdenacaoChange(o); menuOrdenacao = false
                                })
                            }
                        }
                        IconButton(onClick = { menuOpcoes = true }) { Icon(Icons.Default.MoreVert, "Mais opções") }
                        DropdownMenu(expanded = menuOpcoes, onDismissRequest = { menuOpcoes = false }) {
                            DropdownMenuItem(
                                text = { Text("Selecionar itens") },
                                leadingIcon = { Icon(Icons.Default.Checklist, null) },
                                onClick = {
                                    menuOpcoes = false
                                    if (verbetes.isNotEmpty()) viewModel.iniciarSelecao(verbetes.first().id)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Excluir glossário") },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                onClick = {
                                    menuOpcoes = false
                                    confirmarExcluirGlossario = true
                                }
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!uiState.modoSelecao) {
                FloatingActionButton(onClick = { onNovoVerbete(glossarioId) }) {
                    Icon(Icons.Default.Add, "Novo verbete")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            CampoBusca(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                placeholder = "Buscar em palavra, definição, observação, referência, tags"
            )
            ChipsFiltro(
                itens = VerbeteFiltro.entries,
                selecionado = uiState.filtro,
                rotulo = { it.rotulo },
                onSelecionar = viewModel::onFiltroChange,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            if (verbetes.isEmpty()) {
                EstadoVazio("Nenhum verbete encontrado.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(verbetes, key = { it.id }) { verbete ->
                        if (uiState.modoSelecao) {
                            VerbeteSelecionavelItem(
                                verbete = verbete,
                                selecionado = verbete.id in uiState.selecionados,
                                onToggle = { viewModel.alternarSelecao(verbete.id) }
                            )
                        } else {
                            VerbeteSwipeItem(
                                verbete = verbete,
                                onClick = { onVerbeteClick(verbete.glossarioId, verbete.id) },
                                onLongClick = { viewModel.iniciarSelecao(verbete.id) },
                                onFavoritoClick = { viewModel.alternarFavorito(verbete) },
                                onEditar = { onEditarVerbete(verbete.glossarioId, verbete.id) },
                                onExcluir = { verbeteParaExcluir = verbete }
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmação: excluir o glossário inteiro
    if (confirmarExcluirGlossario) {
        ConfirmarDialog(
            titulo = "Excluir glossário",
            mensagem = "Excluir \"${glossario?.nome ?: ""}\" e todos os seus verbetes? " +
                "Esta ação não pode ser desfeita.",
            onConfirmar = {
                confirmarExcluirGlossario = false
                viewModel.excluirGlossario(onExcluido = onVoltar)
            },
            onDismiss = { confirmarExcluirGlossario = false }
        )
    }

    // Confirmação: excluir um verbete
    verbeteParaExcluir?.let { verbete ->
        ConfirmarDialog(
            titulo = "Excluir verbete",
            mensagem = "Excluir o verbete \"${verbete.palavra}\"?",
            onConfirmar = {
                viewModel.excluirVerbete(verbete.id)
                verbeteParaExcluir = null
            },
            onDismiss = { verbeteParaExcluir = null }
        )
    }

    // Confirmação: excluir os verbetes selecionados (lote)
    if (confirmarExcluirSelecionados) {
        val qtd = uiState.selecionados.size
        ConfirmarDialog(
            titulo = "Excluir verbetes",
            mensagem = "Excluir os $qtd verbete(s) selecionado(s)? Esta ação não pode ser desfeita.",
            onConfirmar = {
                confirmarExcluirSelecionados = false
                viewModel.excluirSelecionados()
            },
            onDismiss = { confirmarExcluirSelecionados = false }
        )
    }
}

@Composable
private fun VerbeteSelecionavelItem(
    verbete: Verbete,
    selecionado: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (selecionado) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = selecionado, onCheckedChange = { onToggle() })
            Column(modifier = Modifier.padding(start = 4.dp).weight(1f)) {
                Text(verbete.palavra, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    verbete.definicao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerbeteSwipeItem(
    verbete: Verbete,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoritoClick: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                // Não remove direto: dispara a confirmação e volta o item ao lugar.
                SwipeToDismissBoxValue.EndToStart -> { onExcluir(); false }
                SwipeToDismissBoxValue.StartToEnd -> { onEditar(); false }
                else -> false
            }
        }
    )
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) dismissState.reset()
    }
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val cor = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Transparent
            }
            val alinhamento = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                Alignment.CenterEnd else Alignment.CenterStart
            val icone = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                Icons.Default.Delete else Icons.Default.Edit
            Box(Modifier.fillMaxSize().background(cor).padding(horizontal = 20.dp), contentAlignment = alinhamento) {
                Icon(icone, contentDescription = null)
            }
        }
    ) {
        VerbeteCard(
            verbete = verbete,
            onClick = onClick,
            onLongClick = onLongClick,
            onFavoritoClick = onFavoritoClick,
            onExcluirClick = onExcluir
        )
    }
}
