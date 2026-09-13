package com.farmaciavigor.app.util

import java.text.Normalizer
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Formatos {
    private val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val formatoDigitosData = DateTimeFormatter.ofPattern("ddMMyyyy")
    private val formatoDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")
    private val marcasAcentos = Regex("\\p{Mn}+")

    /** Piso de sanidade para data de nascimento digitada (pega ano trocado, ex.: 0980). */
    private const val ANO_MINIMO = 1900

    /** Datas de nascimento são guardadas como meia-noite UTC (padrão do DatePicker). */
    fun data(millisUtc: Long): String =
        Instant.ofEpochMilli(millisUtc).atZone(ZoneOffset.UTC).toLocalDate().format(formatoData)

    fun dataHora(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatoDataHora)

    fun idade(nascimentoMillisUtc: Long): Int {
        val nascimento = Instant.ofEpochMilli(nascimentoMillisUtc).atZone(ZoneOffset.UTC).toLocalDate()
        return Period.between(nascimento, LocalDate.now()).years.coerceAtLeast(0)
    }

    /** "25031980" → "25/03/1980"; funciona também com a data pela metade, durante a digitação. */
    fun mascaraData(texto: String): String {
        val d = texto.filter { it.isDigit() }.take(8)
        val sb = StringBuilder()
        d.forEachIndexed { i, c ->
            if (i == 2 || i == 4) sb.append('/')
            sb.append(c)
        }
        return sb.toString()
    }

    /** "25031980" → millis UTC (meia-noite). null se estiver incompleta ou não for uma data válida. */
    fun dataParaMillis(digitos: String): Long? {
        val d = digitos.filter { it.isDigit() }
        if (d.length != 8) return null
        val data = try {
            LocalDate.of(d.drop(4).toInt(), d.substring(2, 4).toInt(), d.take(2).toInt())
        } catch (e: DateTimeException) {
            return null
        }
        if (data.year < ANO_MINIMO || data.isAfter(LocalDate.now())) return null
        return data.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    /** Caminho inverso, para preencher o campo ao editar um cadastro. */
    fun millisParaDigitosData(millisUtc: Long): String =
        Instant.ofEpochMilli(millisUtc).atZone(ZoneOffset.UTC).toLocalDate().format(formatoDigitosData)

    /**
     * Erro de uma data digitada, julgando só o que já dá para julgar: enquanto o que foi
     * digitado ainda pode virar uma data válida (ex.: "25/0"), retorna null.
     */
    fun erroData(digitos: String): String? {
        val d = digitos.filter { it.isDigit() }
        if (d.isEmpty()) return null
        val dia = d.take(2).toInt()
        if (d.length >= 2 && (dia == 0 || dia > 31)) return "Dia inválido"
        if (d.length >= 4) {
            val mes = d.substring(2, 4).toInt()
            if (mes == 0 || mes > 12) return "Mês inválido"
        }
        if (d.length < 8) return null
        val ano = d.drop(4).toInt()
        val data = try {
            LocalDate.of(ano, d.substring(2, 4).toInt(), dia)
        } catch (e: DateTimeException) {
            return "Esse dia não existe nesse mês"
        }
        return when {
            ano < ANO_MINIMO -> "Ano inválido"
            data.isAfter(LocalDate.now()) -> "Data no futuro"
            else -> null
        }
    }

    fun mascaraCpf(texto: String): String {
        val d = texto.filter { it.isDigit() }.take(11)
        val sb = StringBuilder()
        d.forEachIndexed { i, c ->
            if (i == 3 || i == 6) sb.append('.')
            if (i == 9) sb.append('-')
            sb.append(c)
        }
        return sb.toString()
    }

    fun mascaraTelefone(texto: String): String {
        val d = texto.filter { it.isDigit() }.take(11)
        return when {
            d.isEmpty() -> ""
            d.length <= 2 -> "($d"
            d.length <= 6 -> "(${d.take(2)}) ${d.drop(2)}"
            d.length <= 10 -> "(${d.take(2)}) ${d.drop(2).dropLast(4)}-${d.takeLast(4)}"
            else -> "(${d.take(2)}) ${d.drop(2).take(5)}-${d.drop(7)}"
        }
    }

    /** Remove acentos e caixa para busca tolerante ("Jose" encontra "José"). */
    fun paraBusca(texto: String): String =
        marcasAcentos.replace(Normalizer.normalize(texto, Normalizer.Form.NFD), "").lowercase()

    /** "maria da silva" → "Maria Da Silva"; o resto da palavra fica como foi digitado. */
    fun capitalizarPalavras(texto: String): String =
        texto.split(" ").joinToString(" ") { palavra -> palavra.replaceFirstChar { it.uppercase() } }

    /** "Cristiane Aparecida da Silva" → "Cristiane". */
    fun primeiroNome(nome: String): String =
        nome.trim().substringBefore(" ").ifBlank { nome.trim() }

    /** Valida os dígitos verificadores do CPF. */
    fun cpfValido(cpf: String): Boolean {
        val d = cpf.filter { it.isDigit() }
        if (d.length != 11) return false
        if (d.all { it == d[0] }) return false
        val n = d.map { it - '0' }
        val dv1 = (0..8).sumOf { n[it] * (10 - it) } * 10 % 11 % 10
        if (dv1 != n[9]) return false
        val dv2 = (0..9).sumOf { n[it] * (11 - it) } * 10 % 11 % 10
        return dv2 == n[10]
    }

    // A composição da injeção é guardada em uma única coluna de texto:
    // uma linha por composto, nome e miligramagem separados por tabulação.

    fun desserializarCompostos(texto: String?): List<Pair<String, String>> =
        texto?.lines()?.filter { it.isNotBlank() }?.map { linha ->
            val partes = linha.split("\t", limit = 2)
            partes[0] to partes.getOrElse(1) { "" }
        } ?: emptyList()

    fun serializarCompostos(compostos: List<Pair<String, String>>): String? =
        compostos
            .map { it.first.trim() to it.second.trim() }
            .filter { it.first.isNotBlank() || it.second.isNotBlank() }
            .joinToString("\n") { "${it.first}\t${it.second}" }
            .ifBlank { null }

    /** "Cianocobalamina\t5.000 mcg" → "Cianocobalamina 5.000 mcg" (para exibição). */
    fun composicaoLegivel(texto: String?): String? =
        desserializarCompostos(texto)
            .joinToString(", ") { composto ->
                listOf(composto.first, composto.second).filter { it.isNotBlank() }.joinToString(" ")
            }
            .ifBlank { null }
}
