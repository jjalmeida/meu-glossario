package com.meuglossario.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.model.Verbete
import com.meuglossario.util.DateUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlossarioCard(
    glossario: Glossario,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(glossario.nome, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${glossario.totalVerbetes} verbete(s) · ${DateUtils.formatarData(glossario.dataCriacao)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun VerbeteCard(
    verbete: Verbete,
    onClick: () -> Unit,
    onFavoritoClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onExcluirClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth().combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(end = 8.dp).weight(1f)) {
                    Text(verbete.palavra, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        verbete.definicao,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onFavoritoClick) {
                    Icon(
                        imageVector = if (verbete.favorito) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorito",
                        tint = if (verbete.favorito) Color(0xFFF5B301) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (onExcluirClick != null) {
                    IconButton(onClick = onExcluirClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir verbete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (verbete.tags.isNotEmpty()) {
                TagChipsRow(tags = verbete.tags, modifier = Modifier.padding(top = 8.dp))
            }
            Text(
                "Criado em ${DateUtils.formatarData(verbete.dataCriacao)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
