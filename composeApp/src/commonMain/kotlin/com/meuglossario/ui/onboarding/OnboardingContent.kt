package com.meuglossario.ui.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

/** Um passo do onboarding / uma novidade do feature discovery. */
data class OnboardingPagina(
    val icone: ImageVector,
    val titulo: String,
    val descricao: String
)

/**
 * Uma novidade associada a uma "versão de features". Ao subir
 * [VERSAO_FEATURES_ATUAL], adicione itens aqui com o número da nova versão.
 */
data class Novidade(
    val versao: Int,
    val icone: ImageVector,
    val titulo: String,
    val descricao: String
)

object OnboardingContent {

    /**
     * Incremente este número sempre que houver novidades a divulgar. Usuários
     * que já viram até a versão anterior receberão o feature discovery.
     */
    const val VERSAO_FEATURES_ATUAL = 2

    /** Telas do onboarding de primeiro acesso (todas as funcionalidades). */
    val paginasOnboarding: List<OnboardingPagina> = listOf(
        OnboardingPagina(
            icone = Icons.AutoMirrored.Filled.MenuBook,
            titulo = "Bem-vindo ao Meu Glossário",
            descricao = "Crie vários glossários e organize seus verbetes (palavra, " +
                "definição, observação e referência) em um só lugar."
        ),
        OnboardingPagina(
            icone = Icons.Default.Search,
            titulo = "Busca avançada",
            descricao = "Pesquise em tempo real por palavra, definição, observação, " +
                "referência e tags — tudo ao mesmo tempo."
        ),
        OnboardingPagina(
            icone = Icons.Default.Label,
            titulo = "Tags e favoritos",
            descricao = "Categorize com tags, marque verbetes como favoritos e filtre " +
                "a lista por Todos, Favoritos ou Recentes."
        ),
        OnboardingPagina(
            icone = Icons.Default.SwapHoriz,
            titulo = "Importar e exportar",
            descricao = "Exporte glossários em JSON e importe arquivos, escolhendo criar " +
                "um novo glossário ou mesclar com um existente."
        ),
        OnboardingPagina(
            icone = Icons.Default.BarChart,
            titulo = "Estatísticas",
            descricao = "Acompanhe totais de glossários, verbetes, favoritos e a " +
                "quantidade de itens por categoria."
        )
    )

    /**
     * Catálogo de novidades por versão. O feature discovery mostra apenas as
     * novidades com versão maior do que a última já vista pelo usuário.
     */
    val novidades: List<Novidade> = listOf(
        Novidade(
            versao = 1,
            icone = Icons.Default.Star,
            titulo = "Campo de referência",
            descricao = "Os verbetes agora têm um campo de referência (fonte), incluído " +
                "também na busca, no compartilhamento e na exportação."
        ),
        Novidade(
            versao = 2,
            icone = Icons.Default.CheckBox,
            titulo = "Seleção múltipla",
            descricao = "Segure um verbete para selecionar vários e excluí-los de uma vez, " +
                "com confirmação."
        ),
        Novidade(
            versao = 2,
            icone = Icons.Default.SwapHoriz,
            titulo = "Importação flexível",
            descricao = "Ao importar um JSON, escolha entre criar um novo glossário ou " +
                "mesclar o conteúdo em um glossário já existente."
        )
    )
}
