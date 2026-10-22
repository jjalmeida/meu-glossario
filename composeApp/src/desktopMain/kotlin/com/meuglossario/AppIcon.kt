package com.meuglossario

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Ícone da aplicação desenhado programaticamente (livro aberto com marcador),
 * usado como ícone da janela no Desktop. Mantém a mesma identidade visual do
 * ícone do Android/Web sem depender de arquivos binários.
 */
class AppIconPainter : Painter() {

    private val azul = Color(0xFF1565C0)
    private val branco = Color(0xFFFFFFFF)
    private val vermelho = Color(0xFFE53935)

    override val intrinsicSize: Size = Size(108f, 108f)

    override fun DrawScope.onDraw() {
        val s = size.minDimension / 108f
        fun x(v: Float) = v * s
        fun y(v: Float) = v * s

        // Fundo arredondado
        drawRoundRect(
            color = azul,
            size = Size(x(108f), y(108f)),
            cornerRadius = CornerRadius(x(24f), y(24f))
        )

        // Página esquerda
        val pagEsq = Path().apply {
            moveTo(x(54f), y(42f))
            cubicTo(x(48f), y(38f), x(40f), y(37f), x(33f), y(38f))
            lineTo(x(33f), y(70f))
            cubicTo(x(40f), y(69f), x(48f), y(70f), x(54f), y(74f))
            close()
        }
        drawPath(pagEsq, branco)

        // Página direita
        val pagDir = Path().apply {
            moveTo(x(54f), y(42f))
            cubicTo(x(60f), y(38f), x(68f), y(37f), x(75f), y(38f))
            lineTo(x(75f), y(70f))
            cubicTo(x(68f), y(69f), x(60f), y(70f), x(54f), y(74f))
            close()
        }
        drawPath(pagDir, branco)

        // Vinco central
        drawRect(azul, topLeft = Offset(x(52.5f), y(42f)), size = Size(x(3f), y(32f)))

        // Linhas de texto
        val linhas = listOf(
            Triple(37f, 46f, 13f), Triple(37f, 51f, 13f), Triple(37f, 56f, 13f), Triple(37f, 61f, 9f),
            Triple(58f, 46f, 13f), Triple(58f, 51f, 13f), Triple(58f, 56f, 13f), Triple(58f, 61f, 9f)
        )
        linhas.forEach { (lx, ly, lw) ->
            drawRect(azul, topLeft = Offset(x(lx), y(ly)), size = Size(x(lw), y(2.4f)))
        }

        // Marcador de página
        val marcador = Path().apply {
            moveTo(x(64f), y(38f))
            lineTo(x(71f), y(38f))
            lineTo(x(71f), y(54f))
            lineTo(x(67.5f), y(50.5f))
            lineTo(x(64f), y(54f))
            close()
        }
        drawPath(marcador, vermelho)
    }
}
