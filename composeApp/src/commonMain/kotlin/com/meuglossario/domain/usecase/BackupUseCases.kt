package com.meuglossario.domain.usecase

import com.meuglossario.data.dto.BackupDto
import com.meuglossario.data.local.currentTimeMillis
import com.meuglossario.data.mapper.toDto
import com.meuglossario.domain.ValidacaoException
import com.meuglossario.domain.repository.GlossarioRepository
import com.meuglossario.domain.repository.VerbeteRepository
import com.meuglossario.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

private val backupJson = Json { prettyPrint = true; ignoreUnknownKeys = true }

/** Serializa todos os glossários (com verbetes, favoritos e tags) para JSON. */
class GerarBackupUseCase(
    private val glossarioRepository: GlossarioRepository,
    private val verbeteRepository: VerbeteRepository
) {
    suspend operator fun invoke(): String {
        val glossarios = glossarioRepository.observarGlossarios().first()
        val dtos = glossarios.map { g -> g.toDto(verbeteRepository.listar(g.id)) }
        val backup = BackupDto(
            versao = 1,
            dataBackup = DateUtils.formatarIso(currentTimeMillis()),
            glossarios = dtos
        )
        return backupJson.encodeToString(BackupDto.serializer(), backup)
    }
}

/** Restaura um backup completo (substituindo glossários existentes). */
class RestaurarBackupUseCase(
    private val importarJsonUseCase: ImportarJsonUseCase
) {
    suspend operator fun invoke(json: String): Int {
        val backup = try {
            backupJson.decodeFromString(BackupDto.serializer(), json)
        } catch (e: Exception) {
            throw ValidacaoException("Backup inválido.")
        }
        var count = 0
        backup.glossarios.forEach { dto ->
            importarJsonUseCase.importarSubstituindo(dto)
            count++
        }
        return count
    }
}
