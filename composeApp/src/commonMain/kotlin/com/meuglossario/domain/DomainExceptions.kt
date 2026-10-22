package com.meuglossario.domain

/** Erro de validação de regras de negócio (nome vazio, duplicado, etc.). */
class ValidacaoException(message: String) : Exception(message)
