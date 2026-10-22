package com.meuglossario.di

import com.meuglossario.data.local.KeyValueStore
import com.meuglossario.data.local.SharedPrefsKeyValueStore
import com.meuglossario.platform.AndroidBackupService
import com.meuglossario.platform.AndroidExportService
import com.meuglossario.platform.AndroidPlatformCapabilities
import com.meuglossario.platform.BackupService
import com.meuglossario.platform.ExportService
import com.meuglossario.platform.PlatformCapabilities
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<KeyValueStore> { SharedPrefsKeyValueStore(androidContext()) }
    single<PlatformCapabilities> { AndroidPlatformCapabilities() }
    single<ExportService> { AndroidExportService(androidContext()) }
    single<BackupService> { AndroidBackupService(androidContext()) }
}
