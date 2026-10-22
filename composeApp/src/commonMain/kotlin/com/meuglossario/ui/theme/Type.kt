package com.meuglossario.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.meuglossario.resources.Res
import com.meuglossario.resources.noto_sans_jp_regular
import org.jetbrains.compose.resources.Font

/**
 * FontFamily com cobertura latina + japonesa (Noto Sans JP).
 *
 * Necessário principalmente no target web (wasmJs): a fonte padrão embutida do
 * Compose não possui glifos CJK, então textos em japonês apareciam como "tofu"
 * (quadrados vazios). No Android/desktop a fonte do sistema já cobre CJK, mas
 * aplicar aqui mantém a renderização consistente entre todas as plataformas.
 */
@Composable
fun notoSansJpFamily(): FontFamily = FontFamily(
    Font(Res.font.noto_sans_jp_regular)
)

/**
 * Typography do Material3 com todos os estilos usando a fonte com suporte a CJK.
 * Reaproveita as métricas/pesos default do Material3, trocando apenas a família.
 */
@Composable
fun appTypography(): Typography {
    val family = notoSansJpFamily()
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family),
        titleMedium = base.titleMedium.copy(fontFamily = family),
        titleSmall = base.titleSmall.copy(fontFamily = family),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family)
    )
}
