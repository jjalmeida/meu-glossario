package com.meuglossario.platform

import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.model.Verbete

/**
 * Serviço de exportação/compartilhamento dependente de plataforma.
 * Cada função retorna uma mensagem de status para exibir ao usuário.
 * No Web, PDF e compartilhamento nativo não são suportados e retornam aviso.
 */
interface ExportService {
    /** Exporta o glossário em PDF (quando suportado). */
    suspend fun exportarPdf(glossario: Glossario, verbetes: List<Verbete>): String

    /** Salva/baixa um conteúdo textual (JSON) com o nome informado. */
    suspend fun salvarTexto(nomeArquivo: String, conteudo: String, mimeType: String): String

    /** Compartilha o texto de um verbete (share nativo ou fallback). */
    suspend fun compartilharVerbete(verbete: Verbete): String

    /** Abre um seletor de arquivo e retorna o conteúdo textual, ou null se cancelado. */
    suspend fun selecionarArquivoTexto(): String?

    /** Monta o texto padrão de um verbete para compartilhamento/cópia. */
    fun textoDoVerbete(verbete: Verbete): String = buildString {
        appendLine("📖 ${verbete.palavra}")
        appendLine()
        appendLine("Definição: ${verbete.definicao}")
        if (verbete.observacao.isNotBlank()) appendLine("Observação: ${verbete.observacao}")
        if (verbete.referencia.isNotBlank()) appendLine("Referência: ${verbete.referencia}")
        if (verbete.tags.isNotEmpty()) appendLine("Tags: ${verbete.tags.joinToString(", ") { it.nome }}")
    }.trim()
}
