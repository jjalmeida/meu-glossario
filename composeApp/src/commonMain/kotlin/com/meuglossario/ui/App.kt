package com.meuglossario.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meuglossario.ui.backup.BackupScreen
import com.meuglossario.ui.detail.VerbeteDetailScreen
import com.meuglossario.ui.edit.VerbeteEditScreen
import com.meuglossario.ui.home.HomeScreen
import com.meuglossario.ui.navigation.Destinos
import com.meuglossario.ui.onboarding.NovidadesDialog
import com.meuglossario.ui.onboarding.OnboardingContent
import com.meuglossario.ui.onboarding.OnboardingScreen
import com.meuglossario.ui.onboarding.OnboardingViewModel
import com.meuglossario.ui.statistics.StatisticsScreen
import com.meuglossario.ui.tags.TagsScreen
import com.meuglossario.ui.theme.MeuGlossarioTheme
import com.meuglossario.ui.verbetes.VerbetesScreen
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinContext {
        MeuGlossarioTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                val onboardingViewModel: OnboardingViewModel = koinViewModel()
                val introState by onboardingViewModel.uiState.collectAsState()

                if (introState.mostrarOnboarding) {
                    OnboardingScreen(
                        paginas = OnboardingContent.paginasOnboarding,
                        onConcluir = onboardingViewModel::concluirOnboarding
                    )
                } else {
                    MeuGlossarioNavGraph()

                    // Feature discovery: novidades após atualização.
                    NovidadesDialog(
                        novidades = introState.novidades,
                        onFechar = onboardingViewModel::concluirNovidades
                    )
                }
            }
        }
    }
}

@Composable
private fun MeuGlossarioNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Destinos.HOME) {

        composable(Destinos.HOME) {
            HomeScreen(
                onGlossarioClick = { navController.navigate(Destinos.verbetes(it)) },
                onEstatisticasClick = { navController.navigate(Destinos.ESTATISTICAS) },
                onTagsClick = { navController.navigate(Destinos.TAGS) },
                onBackupClick = { navController.navigate(Destinos.BACKUP) }
            )
        }

        composable(
            route = Destinos.VERBETES,
            arguments = listOf(navArgument(Destinos.ARG_GLOSSARIO_ID) { type = NavType.LongType })
        ) { entry ->
            val glossarioId = entry.arguments?.getLong(Destinos.ARG_GLOSSARIO_ID) ?: 0L
            VerbetesScreen(
                glossarioId = glossarioId,
                onVoltar = { navController.popBackStack() },
                onVerbeteClick = { gId, vId -> navController.navigate(Destinos.detalheVerbete(gId, vId)) },
                onNovoVerbete = { gId -> navController.navigate(Destinos.editarVerbete(gId, 0)) },
                onEditarVerbete = { gId, vId -> navController.navigate(Destinos.editarVerbete(gId, vId)) }
            )
        }

        composable(
            route = Destinos.DETALHE_VERBETE,
            arguments = listOf(
                navArgument(Destinos.ARG_GLOSSARIO_ID) { type = NavType.LongType },
                navArgument(Destinos.ARG_VERBETE_ID) { type = NavType.LongType }
            )
        ) { entry ->
            val glossarioId = entry.arguments?.getLong(Destinos.ARG_GLOSSARIO_ID) ?: 0L
            val verbeteId = entry.arguments?.getLong(Destinos.ARG_VERBETE_ID) ?: 0L
            VerbeteDetailScreen(
                verbeteId = verbeteId,
                onVoltar = { navController.popBackStack() },
                onEditar = { vId -> navController.navigate(Destinos.editarVerbete(glossarioId, vId)) }
            )
        }

        composable(
            route = Destinos.EDITAR_VERBETE,
            arguments = listOf(
                navArgument(Destinos.ARG_GLOSSARIO_ID) { type = NavType.LongType },
                navArgument(Destinos.ARG_VERBETE_ID) { type = NavType.LongType }
            )
        ) { entry ->
            val glossarioId = entry.arguments?.getLong(Destinos.ARG_GLOSSARIO_ID) ?: 0L
            val verbeteId = entry.arguments?.getLong(Destinos.ARG_VERBETE_ID) ?: 0L
            VerbeteEditScreen(
                glossarioId = glossarioId,
                verbeteId = verbeteId,
                onConcluir = { navController.popBackStack() }
            )
        }

        composable(Destinos.ESTATISTICAS) {
            StatisticsScreen(onVoltar = { navController.popBackStack() })
        }
        composable(Destinos.TAGS) {
            TagsScreen(onVoltar = { navController.popBackStack() })
        }
        composable(Destinos.BACKUP) {
            BackupScreen(onVoltar = { navController.popBackStack() })
        }
    }
}
