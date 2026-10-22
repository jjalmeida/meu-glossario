package com.meuglossario.di

import com.meuglossario.data.local.LocalDataSource
import com.meuglossario.data.prefs.BackupPreferences
import com.meuglossario.data.prefs.OnboardingPreferences
import com.meuglossario.data.repository.GlossarioRepositoryImpl
import com.meuglossario.data.repository.TagRepositoryImpl
import com.meuglossario.data.repository.VerbeteRepositoryImpl
import com.meuglossario.domain.repository.GlossarioRepository
import com.meuglossario.domain.repository.TagRepository
import com.meuglossario.domain.repository.VerbeteRepository
import com.meuglossario.domain.usecase.AlternarFavoritoUseCase
import com.meuglossario.domain.usecase.CriarGlossarioUseCase
import com.meuglossario.domain.usecase.ExcluirGlossarioUseCase
import com.meuglossario.domain.usecase.ExcluirVerbeteUseCase
import com.meuglossario.domain.usecase.ExportarJsonUseCase
import com.meuglossario.domain.usecase.FiltrarVerbetesUseCase
import com.meuglossario.domain.usecase.GerarBackupUseCase
import com.meuglossario.domain.usecase.ImportarJsonUseCase
import com.meuglossario.domain.usecase.ObservarGlossarioUseCase
import com.meuglossario.domain.usecase.ObservarGlossariosUseCase
import com.meuglossario.domain.usecase.ObservarVerbeteUseCase
import com.meuglossario.domain.usecase.ObservarVerbetesUseCase
import com.meuglossario.domain.usecase.ObterEstatisticasUseCase
import com.meuglossario.domain.usecase.RenomearGlossarioUseCase
import com.meuglossario.domain.usecase.RestaurarBackupUseCase
import com.meuglossario.domain.usecase.SalvarVerbeteUseCase
import com.meuglossario.ui.backup.BackupViewModel
import com.meuglossario.ui.detail.VerbeteDetailViewModel
import com.meuglossario.ui.onboarding.OnboardingViewModel
import com.meuglossario.ui.edit.VerbeteEditViewModel
import com.meuglossario.ui.home.HomeViewModel
import com.meuglossario.ui.statistics.StatisticsViewModel
import com.meuglossario.ui.tags.TagsViewModel
import com.meuglossario.ui.verbetes.VerbetesViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Módulo comum: fonte de dados, repositórios, casos de uso e ViewModels.
 * Cada plataforma fornece seu próprio [Module] com KeyValueStore,
 * ExportService, BackupService e PlatformCapabilities.
 */
val commonModule = module {
    single { LocalDataSource(get()) }
    single { BackupPreferences(get()) }
    single { OnboardingPreferences(get()) }

    // Repositórios
    single { TagRepositoryImpl(get()) }
    single<TagRepository> { get<TagRepositoryImpl>() }
    single<GlossarioRepository> { GlossarioRepositoryImpl(get()) }
    single<VerbeteRepository> { VerbeteRepositoryImpl(get(), get<TagRepositoryImpl>()) }

    // Casos de uso
    factory { ObservarGlossariosUseCase(get()) }
    factory { ObservarGlossarioUseCase(get()) }
    factory { CriarGlossarioUseCase(get()) }
    factory { RenomearGlossarioUseCase(get()) }
    factory { ExcluirGlossarioUseCase(get()) }
    factory { ObservarVerbetesUseCase(get()) }
    factory { ObservarVerbeteUseCase(get()) }
    factory { SalvarVerbeteUseCase(get()) }
    factory { ExcluirVerbeteUseCase(get()) }
    factory { AlternarFavoritoUseCase(get()) }
    factory { FiltrarVerbetesUseCase() }
    factory { ObterEstatisticasUseCase(get(), get(), get()) }
    factory { ExportarJsonUseCase(get(), get()) }
    factory { ImportarJsonUseCase(get(), get()) }
    factory { GerarBackupUseCase(get(), get()) }
    factory { RestaurarBackupUseCase(get()) }

    // ViewModels sem parâmetro de rota
    factory { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { StatisticsViewModel(get()) }
    factory { TagsViewModel(get()) }
    factory { OnboardingViewModel(get()) }
    factory {
        BackupViewModel(get(), get(), get(), get(), get(), get(), get(), get())
    }

    // ViewModels com parâmetros de rota (glossarioId / verbeteId)
    factory { (glossarioId: Long) ->
        VerbetesViewModel(glossarioId, get(), get(), get(), get(), get(), get())
    }
    factory { (glossarioId: Long, verbeteId: Long) ->
        VerbeteEditViewModel(glossarioId, verbeteId, get(), get(), get())
    }
    factory { (verbeteId: Long) ->
        VerbeteDetailViewModel(verbeteId, get(), get(), get(), get())
    }
}

/** Fornecido por cada plataforma. */
expect val platformModule: Module

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) {
    startKoin {
        appDeclaration()
        modules(commonModule, platformModule)
    }
}
