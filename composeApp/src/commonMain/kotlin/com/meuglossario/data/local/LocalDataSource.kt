package com.meuglossario.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Registro persistido de um glossário. */
@Serializable
data class GlossarioRecord(
    val id: Long,
    val nome: String,
    val dataCriacao: Long
)

/** Registro persistido de um verbete (tags por id). */
@Serializable
data class VerbeteRecord(
    val id: Long,
    val glossarioId: Long,
    val palavra: String,
    val definicao: String,
    val observacao: String = "",
    val referencia: String = "",
    val favorito: Boolean = false,
    val dataCriacao: Long = 0,
    val tagIds: List<Long> = emptyList()
)

/** Registro persistido de uma tag. */
@Serializable
data class TagRecord(
    val id: Long,
    val nome: String
)

/** Estado completo do "banco" serializável. */
@Serializable
data class DatabaseState(
    val glossarios: List<GlossarioRecord> = emptyList(),
    val verbetes: List<VerbeteRecord> = emptyList(),
    val tags: List<TagRecord> = emptyList(),
    val seqGlossario: Long = 0,
    val seqVerbete: Long = 0,
    val seqTag: Long = 0
)

/**
 * Fonte de dados única em memória, com persistência via [KeyValueStore].
 * Todos os repositórios operam sobre este estado, garantindo cache reativo
 * (StateFlow) e persistência idêntica em Android, Desktop e Web.
 */
class LocalDataSource(private val store: KeyValueStore) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _state = MutableStateFlow(carregar())
    val state: StateFlow<DatabaseState> = _state.asStateFlow()

    private fun carregar(): DatabaseState {
        val raw = store.getString(KEY) ?: return DatabaseState()
        return try {
            json.decodeFromString(DatabaseState.serializer(), raw)
        } catch (e: Exception) {
            DatabaseState()
        }
    }

    /** Atualiza o estado de forma atômica e persiste. */
    fun update(transform: (DatabaseState) -> DatabaseState) {
        val novo = transform(_state.value)
        _state.value = novo
        store.putString(KEY, json.encodeToString(DatabaseState.serializer(), novo))
    }

    fun snapshot(): DatabaseState = _state.value

    companion object {
        private const val KEY = "meu_glossario_db"
    }
}
