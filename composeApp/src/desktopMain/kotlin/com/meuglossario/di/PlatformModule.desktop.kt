package com.meuglossario.di

import com.meuglossario.data.local.FileKeyValueStore
import com.meuglossario.data.local.KeyValueStore
import com.meuglossario.platform.BackupService
import com.meuglossario.platform.DesktopBackupService
import com.meuglossario.platform.DesktopExportService
import com.meuglossario.platform.DesktopPlatformCapabilities
import com.meuglossario.platform.ExportService
import com.meuglossario.platform.PlatformCapabilities
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<KeyValueStore> { FileKeyValueStore() }
    single<PlatformCapabilities> { DesktopPlatformCapabilities() }
    single<ExportService> { DesktopExportService() }
    single<BackupService> { DesktopBackupService() }
}
