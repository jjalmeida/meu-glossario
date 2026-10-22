package com.meuglossario.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Formatação de datas multiplataforma a partir de epoch millis. */
object DateUtils {

    private fun dois(n: Int): String = if (n < 10) "0$n" else n.toString()

    fun formatarData(epochMillis: Long?): String {
        if (epochMillis == null || epochMillis == 0L) return "-"
        val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dois(dt.dayOfMonth)}/${dois(dt.monthNumber)}/${dt.year}"
    }

    fun formatarDataHora(epochMillis: Long?): String {
        if (epochMillis == null || epochMillis == 0L) return "-"
        val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dois(dt.dayOfMonth)}/${dois(dt.monthNumber)}/${dt.year} ${dois(dt.hour)}:${dois(dt.minute)}"
    }

    /** Formato ISO simples (yyyy-MM-dd) usado no JSON. */
    fun formatarIso(epochMillis: Long): String {
        val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.year}-${dois(dt.monthNumber)}-${dois(dt.dayOfMonth)}"
    }
}
