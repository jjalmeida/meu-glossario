package com.meuglossario.data.local

import android.content.Context

/** Implementação Android usando SharedPreferences. */
class SharedPrefsKeyValueStore(context: Context) : KeyValueStore {
    private val prefs = context.getSharedPreferences("meu_glossario_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
