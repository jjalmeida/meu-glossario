package com.meuglossario.domain.usecase

import com.meuglossario.data.dto.GlossarioDto
import com.meuglossario.data.mapper.toDto
import com.meuglossario.domain.ValidacaoException
import com.meuglossario.domain.model.Verbete
import com.meuglossario.domain.repository.GlossarioRepository
import com.meuglossario.domain.repository.VerbeteRepository
import com.meuglossario.data.local.currentTimeMillis
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Resultado da validação/pré-visualização de um arquivo JSON. */
data class PreviewImportacao(
    val dto: GlossarioDto,
    val totalVerbetes: Int
)

private val jsonPretty = Json { prettyPrint = true; ignoreUnknownKeys = true }
private val jsonParser = Json { ignoreUnknownKeys = true }

/** Gera a representação JSON de um glossário completo. */
class ExportarJsonUseCase(
    private val glossarioRepository: GlossarioRepository,
    private val verbeteRepository: VerbeteRepository
) {
    suspend operator fun invoke(glossarioId: Long): String {
        val glossario = glossarioRepository.buscarPorId(glossarioId)
            ?: throw ValidacaoException("Glossário não encontrado.")
        val verbetes = verbeteRepository.listar(glossarioId)
        return jsonPretty.encodeToString(GlossarioDto.serializer(), glossario.toDto(verbetes))
    }
}

/** Valida e importa um glossário a partir de JSON, com pré-visualização. */
class ImportarJsonUseCase(
    private val glossarioRepository: GlossarioRepository,
    private val verbeteRepository: VerbeteRepository
) {
    suspend fun preVisualizar(json: String): PreviewImportacao {
        val dto = parse(json)
        return PreviewImportacao(dto, dto.verbetes.size)
    }

    /**
     * Importa criando um novo glossário. Se já existir um com o mesmo nome,
     * gera um nome único (ex.: "Nome (2)").
     */
    suspend fun importarComoNovo(dto: GlossarioDto): Long {
        val glossarioId = glossarioRepository.criar(gerarNomeUnico(dto.nome))
        adicionarVerbetes(dto, glossarioId, pularExistentes = false)
        return glossarioId
    }

    /**
     * Mescla os verbetes do JSON em um glossário existente escolhido pelo
     * usuário, pulando palavras que já existem nele.
     */
    suspend fun importarMesclando(dto: GlossarioDto, glossarioIdDestino: Long): Long {
        adicionarVerbetes(dto, glossarioIdDestino, pularExistentes = true)
        return glossarioIdDestino
    }

    /**
     * Importa substituindo um glossário de mesmo nome (usado na restauração de
     * backup). Se não existir, cria.
     */
    suspend fun importarSubstituindo(dto: GlossarioDto): Long {
        val existenteId = glossarioRepository.idPorNome(dto.nome)
        if (existenteId != null) glossarioRepository.excluir(existenteId)
        val glossarioId = glossarioRepository.criar(dto.nome)
        adicionarVerbetes(dto, glossarioId, pularExistentes = false)
        return glossarioId
    }

    private suspend fun adicionarVerbetes(
        dto: GlossarioDto,
        glossarioId: Long,
        pularExistentes: Boolean
    ) {
        val existentes: Set<String> = if (pularExistentes) {
            verbeteRepository.listar(glossarioId).map { it.palavra.lowercase() }.toSet()
        } else emptySet()

        dto.verbetes.forEach { v ->
            if (v.palavra.lowercase() in existentes) return@forEach
            val verbete = Verbete(
                glossarioId = glossarioId,
                palavra = v.palavra,
                definicao = v.definicao,
                observacao = v.observacao,
                referencia = v.referencia,
                favorito = v.favorito,
                dataCriacao = currentTimeMillis()
            )
            verbeteRepository.salvar(verbete, v.tags)
        }
    }

    private suspend fun gerarNomeUnico(base: String): String {
        val nomeBase = base.trim().ifEmpty { "Glossário importado" }
        if (glossarioRepository.idPorNome(nomeBase) == null) return nomeBase
        var i = 2
        while (glossarioRepository.idPorNome("$nomeBase ($i)") != null) i++
        return "$nomeBase ($i)"
    }

    private fun parse(json: String): GlossarioDto {
        val dto = try {
            jsonParser.decodeFromString(GlossarioDto.serializer(), json)
        } catch (e: SerializationException) {
            throw ValidacaoException("Arquivo JSON inválido.")
        }
        if (dto.nome.isBlank()) {
            throw ValidacaoException("O JSON não possui um nome de glossário válido.")
        }
        if (dto.verbetes.any { it.palavra.isBlank() || it.definicao.isBlank() }) {
            throw ValidacaoException("Há verbetes sem palavra ou definição no arquivo.")
        }
        return dto
    }
}
