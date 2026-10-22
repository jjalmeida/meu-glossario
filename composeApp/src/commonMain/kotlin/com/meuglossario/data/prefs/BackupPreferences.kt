package com.meuglossario.data.prefs

import com.meuglossario.data.local.KeyValueStore

/** Preferências de backup persistidas no KeyValueStore. */
class BackupPreferences(private val store: KeyValueStore) {

    var ultimoBackup: Long?
        get() = store.getString(KEY_ULTIMO)?.toLongOrNull()
        set(value) {
            if (value == null) store.remove(KEY_ULTIMO) else store.putString(KEY_ULTIMO, value.toString())
        }

    var autoBackup: Boolean
        get() = store.getString(KEY_AUTO) == "true"
        set(value) = store.putString(KEY_AUTO, value.toString())

    companion object {
        private const val KEY_ULTIMO = "backup_ultimo"
        private const val KEY_AUTO = "backup_auto"
    }
}
