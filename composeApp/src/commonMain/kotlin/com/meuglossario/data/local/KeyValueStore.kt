package com.meuglossario.data.local

/**
 * Armazenamento chave-valor simples e multiplataforma.
 * - Web (Wasm): localStorage
 * - Desktop (JVM): arquivo JSON no diretório do usuário
 * - Android: SharedPreferences
 *
 * A camada de repositório serializa o estado completo (glossários, verbetes,
 * tags) e persiste como strings sob poucas chaves.
 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

/** Retorna o instante atual em epoch millis (implementado por plataforma). */
expect fun currentTimeMillis(): Long
