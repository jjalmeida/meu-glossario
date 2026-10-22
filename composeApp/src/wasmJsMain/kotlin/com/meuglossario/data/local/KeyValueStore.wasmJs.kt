package com.meuglossario.data.local

import kotlinx.browser.localStorage

/** Implementação Web usando window.localStorage. */
class LocalStorageKeyValueStore : KeyValueStore {
    override fun getString(key: String): String? = localStorage.getItem(key)
    override fun putString(key: String, value: String) = localStorage.setItem(key, value)
    override fun remove(key: String) = localStorage.removeItem(key)
}

private fun jsNow(): Double = js("Date.now()")

actual fun currentTimeMillis(): Long = jsNow().toLong()
