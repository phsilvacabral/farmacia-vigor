package com.farmaciavigor.app.dados

import androidx.room.withTransaction

class Repositorio(private val banco: BancoDeDados) {

    data class ResultadoRegistro(
        val tratamentoId: Long,
        val adicionadaEmTratamentoExistente: Boolean,
        val tratamentoConcluido: Boolean,
    )

    /**
     * Registra uma aplicação. Se o cliente já tem tratamento ativo com esta
     * injeção, a aplicação entra nele; caso contrário um novo tratamento é
     * criado com [totalDoses] e a forma de pagamento informada.
     */
    suspend fun registrarAplicacao(
        clienteId: Long,
        injecaoId: Long,
        totalDoses: Int,
        pagamentoAntecipado: Boolean,
        aplicacaoPaga: Boolean,
        funcionarioId: Long,
    ): ResultadoRegistro = banco.withTransaction {
        val existente = banco.tratamentoDao().ativoDoPar(clienteId, injecaoId)
        val tratamento = existente ?: run {
            val novo = Tratamento(
                clienteId = clienteId,
                injecaoId = injecaoId,
                totalDoses = totalDoses,
                pagamentoAntecipado = pagamentoAntecipado,
                iniciadoEm = System.currentTimeMillis(),
            )
            novo.copy(id = banco.tratamentoDao().inserir(novo))
        }
        val concluido = inserirAplicacao(tratamento, funcionarioId, aplicacaoPaga)
        ResultadoRegistro(tratamento.id, existente != null, concluido)
    }

    /**
     * Abre um tratamento sem registrar aplicação — fica salvo para o cliente
     * aguardando a 1ª aplicação, que depois entra nele via [registrarAplicacao].
     * Se já existe um tratamento ativo deste par cliente+injeção, devolve o dele.
     */
    suspend fun abrirTratamento(
        clienteId: Long,
        injecaoId: Long,
        totalDoses: Int,
        pagamentoAntecipado: Boolean,
    ): Long = banco.withTransaction {
        banco.tratamentoDao().ativoDoPar(clienteId, injecaoId)?.id
            ?: banco.tratamentoDao().inserir(
                Tratamento(
                    clienteId = clienteId,
                    injecaoId = injecaoId,
                    totalDoses = totalDoses,
                    pagamentoAntecipado = pagamentoAntecipado,
                    iniciadoEm = System.currentTimeMillis(),
                )
            )
    }

    /** Registra a próxima aplicação de um tratamento já aberto. Retorna true se o tratamento foi concluído. */
    suspend fun registrarNoTratamento(
        tratamentoId: Long,
        funcionarioId: Long,
        aplicacaoPaga: Boolean,
    ): Boolean = banco.withTransaction {
        val tratamento = banco.tratamentoDao().porId(tratamentoId)
            ?: return@withTransaction false
        inserirAplicacao(tratamento, funcionarioId, aplicacaoPaga)
    }

    private suspend fun inserirAplicacao(
        tratamento: Tratamento,
        funcionarioId: Long,
        paga: Boolean,
    ): Boolean {
        banco.aplicacaoDao().inserir(
            Aplicacao(
                tratamentoId = tratamento.id,
                funcionarioId = funcionarioId,
                dataHora = System.currentTimeMillis(),
                pago = if (tratamento.pagamentoAntecipado) true else paga,
            )
        )
        val aplicadas = banco.aplicacaoDao().contarDoTratamento(tratamento.id)
        val concluido = aplicadas >= tratamento.totalDoses
        if (concluido && !tratamento.encerrado) {
            banco.tratamentoDao().atualizar(tratamento.copy(encerrado = true))
        }
        return concluido
    }

    suspend fun encerrarTratamento(tratamentoId: Long) {
        banco.tratamentoDao().porId(tratamentoId)?.let {
            banco.tratamentoDao().atualizar(it.copy(encerrado = true))
        }
    }
}
