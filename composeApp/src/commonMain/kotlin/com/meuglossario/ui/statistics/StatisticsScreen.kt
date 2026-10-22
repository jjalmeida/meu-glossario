package com.meuglossario.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.meuglossario.util.DateUtils
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onVoltar: () -> Unit,
    viewModel: StatisticsViewModel = koinViewModel()
) {
    val stats by viewModel.estatisticas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estatísticas") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardStat("Glossários", stats.totalGlossarios.toString(), Modifier.weight(1f))
                CardStat("Verbetes", stats.totalVerbetes.toString(), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardStat("Favoritos", stats.totalFavoritos.toString(), Modifier.weight(1f))
                CardStat("Categorias", stats.contagemPorTag.size.toString(), Modifier.weight(1f))
            }
            CardLinha("Glossário com mais verbetes",
                stats.glossarioComMaisVerbetes?.let { "${it.nome} (${it.totalVerbetes})" } ?: "-")
            CardLinha("Último verbete criado", DateUtils.formatarDataHora(stats.dataUltimoVerbete))

            Text("Quantidade por categoria", style = MaterialTheme.typography.titleMedium)
            if (stats.contagemPorTag.isEmpty()) {
                Text("Nenhuma tag cadastrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                stats.contagemPorTag.forEach { CardLinha(it.nome, "${it.total} verbete(s)") }
            }
        }
    }
}

@Composable
private fun CardStat(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(titulo, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CardLinha(titulo: String, valor: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(titulo)
            Text(valor, fontWeight = FontWeight.SemiBold)
        }
    }
}
