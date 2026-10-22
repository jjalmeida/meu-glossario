package com.meuglossario.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.meuglossario.ui.components.CarregandoCentralizado
import com.meuglossario.ui.components.ConfirmarDialog
import com.meuglossario.ui.components.TagChipsRow
import com.meuglossario.util.DateUtils
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbeteDetailScreen(
    verbeteId: Long,
    onVoltar: () -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: VerbeteDetailViewModel = koinViewModel { parametersOf(verbeteId) }
) {
    val verbete by viewModel.verbete.collectAsState()
    val mensagem by viewModel.mensagem.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmar by remember { mutableStateOf(false) }

    LaunchedEffect(mensagem) {
        mensagem?.let { snackbarHostState.showSnackbar(it.texto); viewModel.mensagemConsumida() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalhes") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                },
                actions = {
                    verbete?.let { v ->
                        IconButton(onClick = viewModel::alternarFavorito) {
                            Icon(
                                if (v.favorito) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                "Favorito",
                                tint = if (v.favorito) Color(0xFFF5B301) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = viewModel::compartilhar) { Icon(Icons.Default.Share, "Compartilhar") }
                        IconButton(onClick = { onEditar(v.id) }) { Icon(Icons.Default.Edit, "Editar") }
                        IconButton(onClick = { confirmar = true }) { Icon(Icons.Default.Delete, "Excluir") }
                    }
                }
            )
        }
    ) { padding ->
        val v = verbete
        if (v == null) {
            CarregandoCentralizado(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(v.palavra, style = MaterialTheme.typography.titleLarge)
                Text(
                    "Criado em ${DateUtils.formatarDataHora(v.dataCriacao)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Secao("Definição", v.definicao)
                if (v.observacao.isNotBlank()) Secao("Observação", v.observacao)
                if (v.referencia.isNotBlank()) Secao("Referência", v.referencia)
                if (v.tags.isNotEmpty()) {
                    Text("Tags", style = MaterialTheme.typography.titleMedium)
                    TagChipsRow(tags = v.tags)
                }
            }
        }
    }

    if (confirmar) {
        ConfirmarDialog(
            titulo = "Excluir verbete",
            mensagem = "Deseja excluir este verbete?",
            onConfirmar = { confirmar = false; viewModel.excluir(onExcluido = onVoltar) },
            onDismiss = { confirmar = false }
        )
    }
}

@Composable
private fun Secao(titulo: String, conteudo: String) {
    Column {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Text(conteudo, style = MaterialTheme.typography.bodyLarge)
    }
}
