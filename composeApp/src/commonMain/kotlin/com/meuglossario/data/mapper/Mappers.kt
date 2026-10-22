package com.meuglossario.data.mapper

import com.meuglossario.data.dto.GlossarioDto
import com.meuglossario.data.dto.VerbeteDto
import com.meuglossario.domain.model.Glossario
import com.meuglossario.domain.model.Verbete
import com.meuglossario.util.DateUtils

fun Verbete.toDto(): VerbeteDto = VerbeteDto(
    palavra = palavra,
    definicao = definicao,
    observacao = observacao,
    referencia = referencia,
    favorito = favorito,
    tags = tags.map { it.nome }
)

fun Glossario.toDto(verbetes: List<Verbete>): GlossarioDto = GlossarioDto(
    nome = nome,
    dataCriacao = DateUtils.formatarIso(if (dataCriacao == 0L) 0L else dataCriacao),
    verbetes = verbetes.map { it.toDto() }
)
