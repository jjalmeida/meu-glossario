package com.meuglossario.platform

import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.model.Verbete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.awt.datatransfer.StringSelection
import java.awt.Toolkit
import java.io.File

/**
 * Desktop: PDF indisponível nesta versão; JSON é salvo via diálogo de arquivo;
 * "compartilhar" copia o texto para a área de transferência.
 */
class DesktopExportService : ExportService {

    override suspend fun exportarPdf(glossario: Glossario, verbetes: List<Verbete>): String =
        "Exportação em PDF não está disponível na versão desktop."

    override suspend fun salvarTexto(nomeArquivo: String, conteudo: String, mimeType: String): String =
        withContext(Dispatchers.IO) {
            val dialog = FileDialog(null as Frame?, "Salvar", FileDialog.SAVE).apply {
                file = nomeArquivo
                isVisible = true
            }
            val dir = dialog.directory
            val nome = dialog.file
            if (dir == null || nome == null) {
                "Exportação cancelada."
            } else {
                File(dir, nome).writeText(conteudo)
                "Arquivo salvo em ${File(dir, nome).absolutePath}"
            }
        }

    override suspend fun compartilharVerbete(verbete: Verbete): String {
        val texto = textoDoVerbete(verbete)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(texto), null)
        return "Texto do verbete copiado para a área de transferência."
    }

    override suspend fun selecionarArquivoTexto(): String? = withContext(Dispatchers.IO) {
        val dialog = FileDialog(null as Frame?, "Abrir JSON", FileDialog.LOAD).apply {
            isVisible = true
        }
        val dir = dialog.directory
        val nome = dialog.file
        if (dir == null || nome == null) null else File(dir, nome).readText()
    }
}
