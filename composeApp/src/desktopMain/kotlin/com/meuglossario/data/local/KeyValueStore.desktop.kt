package com.meuglossario.data.local

import java.io.File
import java.util.Properties

/**
 * Implementação Desktop: persiste em um arquivo de propriedades no diretório
 * do usuário (~/.meuglossario/store.properties).
 */
class FileKeyValueStore : KeyValueStore {
    private val file: File = File(
        System.getProperty("user.home"),
        ".meuglossario/store.properties"
    ).also { it.parentFile?.mkdirs() }

    private val props = Properties().apply {
        if (file.exists()) file.inputStream().use { load(it) }
    }

    override fun getString(key: String): String? = props.getProperty(key)

    override fun putString(key: String, value: String) {
        props.setProperty(key, value)
        file.outputStream().use { props.store(it, "Meu Glossário") }
    }

    override fun remove(key: String) {
        props.remove(key)
        file.outputStream().use { props.store(it, "Meu Glossário") }
    }
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
