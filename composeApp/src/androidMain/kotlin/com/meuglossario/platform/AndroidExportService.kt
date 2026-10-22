package com.meuglossario.platform

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.model.Verbete
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android: gera PDF com PdfDocument, salva/baixa arquivos e usa Intents de
 * compartilhamento. A seleção de arquivo para importação é feita pela Activity
 * (registrada via [AndroidFileBridge]).
 */
class AndroidExportService(
    private val context: Context
) : ExportService {

    private val authority = "${context.packageName}.fileprovider"

    override suspend fun exportarPdf(glossario: Glossario, verbetes: List<Verbete>): String =
        withContext(Dispatchers.IO) {
            val file = gerarPdf(glossario, verbetes)
            compartilharArquivo(file, "application/pdf")
            "PDF gerado: ${file.name}"
        }

    override suspend fun salvarTexto(nomeArquivo: String, conteudo: String, mimeType: String): String =
        withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(dir, sanitize(nomeArquivo))
            file.writeText(conteudo)
            compartilharArquivo(file, mimeType)
            "Arquivo gerado: ${file.name}"
        }

    override suspend fun compartilharVerbete(verbete: Verbete): String {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, verbete.palavra)
            putExtra(Intent.EXTRA_TEXT, textoDoVerbete(verbete))
        }
        iniciar(Intent.createChooser(intent, "Compartilhar verbete"))
        return "Compartilhando verbete..."
    }

    override suspend fun selecionarArquivoTexto(): String? {
        val bridge = AndroidFileBridge.instance ?: return null
        val uri = bridge.pickFile() ?: return null
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
        }
    }

    private fun compartilharArquivo(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        iniciar(Intent.createChooser(intent, "Compartilhar"))
    }

    private fun iniciar(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    private fun gerarPdf(glossario: Glossario, verbetes: List<Verbete>): File {
        val doc = PdfDocument()
        val titlePaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 20f }
        val bodyPaint = Paint().apply { textSize = 12f }
        val boldPaint = Paint().apply { typeface = Typeface.DEFAULT_BOLD; textSize = 14f }
        val width = 595; val height = 842; val margin = 40f; val line = 18f

        var page = doc.startPage(PdfDocument.PageInfo.Builder(width, height, 1).create())
        var canvas = page.canvas
        var y = margin + 10
        var pageNum = 1

        canvas.drawText(glossario.nome, margin, y, titlePaint); y += line + 6
        canvas.drawText("Total de verbetes: ${verbetes.size}", margin, y, bodyPaint); y += line + 10

        verbetes.forEachIndexed { i, v ->
            if (y > height - margin - line * 4) {
                doc.finishPage(page)
                pageNum++
                page = doc.startPage(PdfDocument.PageInfo.Builder(width, height, pageNum).create())
                canvas = page.canvas
                y = margin + 10
            }
            val estrela = if (v.favorito) "★ " else ""
            canvas.drawText("${i + 1}. $estrela${v.palavra}", margin, y, boldPaint); y += line
            canvas.drawText("Definição: ${v.definicao}", margin, y, bodyPaint); y += line
            if (v.observacao.isNotBlank()) { canvas.drawText("Observação: ${v.observacao}", margin, y, bodyPaint); y += line }
            if (v.referencia.isNotBlank()) { canvas.drawText("Referência: ${v.referencia}", margin, y, bodyPaint); y += line }
            if (v.tags.isNotEmpty()) { canvas.drawText("Tags: ${v.tags.joinToString(", ") { it.nome }}", margin, y, bodyPaint); y += line }
            y += line
        }
        doc.finishPage(page)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, sanitize("${glossario.nome}.pdf"))
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    private fun sanitize(name: String) =
        name.replace(Regex("[^a-zA-Z0-9-_. ]"), "_").trim().ifEmpty { "arquivo" }
}

/**
 * Ponte para a Activity fornecer seleção de arquivo (SAF) de forma suspensa.
 * A Activity registra um launcher e completa o [pending] quando o usuário escolhe.
 */
object AndroidFileBridge {
    @Volatile var instance: FilePicker? = null

    interface FilePicker {
        suspend fun pickFile(): android.net.Uri?
    }
}
