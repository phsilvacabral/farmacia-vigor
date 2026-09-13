package com.farmaciavigor.app.dados

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "funcionarios")
data class Funcionario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val sexo: String,
)

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nomeCompleto: String,
    val cpf: String? = null,
    /** Data de nascimento em milissegundos UTC (meia-noite). */
    val dataNascimento: Long,
    val sexo: String,
    val telefone: String? = null,
)

@Entity(tableName = "injecoes")
data class Injecao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nomeComercial: String,
    val composicao: String? = null,
    val laboratorio: String? = null,
    /** Nome do arquivo da foto dentro de filesDir/fotos. */
    val fotoArquivo: String? = null,
    /** Quantas aplicações compõem um tratamento com esta injeção. */
    val qtdAplicacoes: Int,
)

/**
 * Série de aplicações de uma injeção em um cliente. Criado automaticamente
 * na primeira aplicação e encerrado quando todas as doses foram aplicadas.
 */
@Entity(tableName = "tratamentos")
data class Tratamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clienteId: Long,
    val injecaoId: Long,
    val totalDoses: Int,
    /** true = pagou tudo antecipado; false = paga a cada aplicação. */
    val pagamentoAntecipado: Boolean,
    val iniciadoEm: Long,
    val encerrado: Boolean = false,
)

@Entity(tableName = "aplicacoes")
data class Aplicacao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tratamentoId: Long,
    val funcionarioId: Long,
    val dataHora: Long,
    val pago: Boolean,
)

/** Tratamento com os dados agregados exibidos nas listas. */
data class TratamentoResumo(
    @Embedded val tratamento: Tratamento,
    val clienteNome: String,
    val injecaoNome: String,
    val aplicadas: Int,
    val naoPagas: Int,
    val ultimaAplicacao: Long?,
) {
    val concluido: Boolean get() = aplicadas >= tratamento.totalDoses

    /** Tratamento aberto e salvo para o cliente, mas nenhuma dose aplicada ainda. */
    val aguardandoPrimeira: Boolean get() = aplicadas == 0 && !tratamento.encerrado
}

/** Uma aplicação como aparece no histórico do cliente. */
data class AplicacaoHistorico(
    @Embedded val aplicacao: Aplicacao,
    val injecaoNome: String,
    val funcionarioNome: String,
    val pagamentoAntecipado: Boolean,
)
