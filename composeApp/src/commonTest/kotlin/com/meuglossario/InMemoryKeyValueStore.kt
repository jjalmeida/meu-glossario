package com.meuglossario

import com.meuglossario.data.local.KeyValueStore

/** KeyValueStore em memória para os testes multiplataforma. */
class InMemoryKeyValueStore : KeyValueStore {
    private val map = mutableMapOf<String, String>()
    override fun getString(key: String): String? = map[key]
    override fun putString(key: String, value: String) { map[key] = value }
    override fun remove(key: String) { map.remove(key) }
}
