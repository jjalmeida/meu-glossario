package com.meuglossario.di

import com.meuglossario.data.local.KeyValueStore
import com.meuglossario.data.local.LocalStorageKeyValueStore
import com.meuglossario.platform.BackupService
import com.meuglossario.platform.ExportService
import com.meuglossario.platform.PlatformCapabilities
import com.meuglossario.platform.WebBackupService
import com.meuglossario.platform.WebExportService
import com.meuglossario.platform.WebPlatformCapabilities
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<KeyValueStore> { LocalStorageKeyValueStore() }
    single<PlatformCapabilities> { WebPlatformCapabilities() }
    single<ExportService> { WebExportService() }
    single<BackupService> { WebBackupService() }
}
