package com.meuglossario.platform

import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.model.Verbete
import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * Web: PDF e compartilhamento nativo indisponíveis.
 * Exportar JSON dispara download; importar usa <input type=file>.
 * As operações de DOM são feitas por pequenas funções JS para evitar
 * fricção de tipagem no Kotlin/Wasm.
 */

// ---- Bridges JS ----

private fun jsBaixarTexto(nome: String, conteudo: String, mime: String): Unit =
    js(
        """{
            const blob = new Blob([conteudo], { type: mime });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url; a.download = nome; a.click();
            URL.revokeObjectURL(url);
        }"""
    )

private fun jsCopiarClipboard(texto: String): Unit =
    js("{ if (navigator.clipboard) navigator.clipboard.writeText(texto); }")

private fun jsSelecionarArquivo(): Promise<JsString?> =
    js(
        """(new Promise(function(resolve) {
            const input = document.createElement('input');
            input.type = 'file';
            input.accept = '.json,application/json,text/plain';
            input.onchange = function() {
                const file = input.files && input.files[0];
                if (!file) { resolve(null); return; }
                const reader = new FileReader();
                reader.onload = function() { resolve(reader.result); };
                reader.onerror = function() { resolve(null); };
                reader.readAsText(file);
            };
            input.click();
        }))"""
    )

class WebExportService : ExportService {

    override suspend fun exportarPdf(glossario: Glossario, verbetes: List<Verbete>): String =
        "Exportação em PDF não está disponível na versão web."

    override suspend fun salvarTexto(nomeArquivo: String, conteudo: String, mimeType: String): String {
        jsBaixarTexto(nomeArquivo, conteudo, mimeType)
        return "Download iniciado: $nomeArquivo"
    }

    override suspend fun compartilharVerbete(verbete: Verbete): String {
        jsCopiarClipboard(textoDoVerbete(verbete))
        return "Texto do verbete copiado para a área de transferência."
    }

    override suspend fun selecionarArquivoTexto(): String? =
        jsSelecionarArquivo().await<JsString?>()?.toString()
}
