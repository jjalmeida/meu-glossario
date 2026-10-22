package com.meuglossario

import android.app.Application
import com.meuglossario.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MeuGlossarioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@MeuGlossarioApp)
        }
    }
}
