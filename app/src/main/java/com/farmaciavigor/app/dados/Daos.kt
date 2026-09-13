package com.farmaciavigor.app.dados

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FuncionarioDao {
    @Query("SELECT * FROM funcionarios ORDER BY nome COLLATE NOCASE")
    fun todos(): Flow<List<Funcionario>>

    @Insert
    suspend fun inserir(funcionario: Funcionario): Long
}

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes ORDER BY nomeCompleto COLLATE NOCASE")
    fun todos(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE id = :id")
    fun porIdFlow(id: Long): Flow<Cliente?>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun porId(id: Long): Cliente?

    @Insert
    suspend fun inserir(cliente: Cliente): Long

    @Update
    suspend fun atualizar(cliente: Cliente)
}

@Dao
interface InjecaoDao {
    @Query("SELECT * FROM injecoes ORDER BY nomeComercial COLLATE NOCASE")
    fun todas(): Flow<List<Injecao>>

    @Query("SELECT * FROM injecoes WHERE id = :id")
    suspend fun porId(id: Long): Injecao?

    @Insert
    suspend fun inserir(injecao: Injecao): Long

    @Update
    suspend fun atualizar(injecao: Injecao)
}

@Dao
interface TratamentoDao {
    @Query(
        """
        SELECT t.*, c.nomeCompleto AS clienteNome, i.nomeComercial AS injecaoNome,
            (SELECT COUNT(*) FROM aplicacoes a WHERE a.tratamentoId = t.id) AS aplicadas,
            (SELECT COUNT(*) FROM aplicacoes a WHERE a.tratamentoId = t.id AND a.pago = 0) AS naoPagas,
            (SELECT MAX(a.dataHora) FROM aplicacoes a WHERE a.tratamentoId = t.id) AS ultimaAplicacao
        FROM tratamentos t
        JOIN clientes c ON c.id = t.clienteId
        JOIN injecoes i ON i.id = t.injecaoId
        WHERE t.encerrado = 0
        ORDER BY c.nomeCompleto COLLATE NOCASE
        """
    )
    fun ativos(): Flow<List<TratamentoResumo>>

    @Query(
        """
        SELECT t.*, c.nomeCompleto AS clienteNome, i.nomeComercial AS injecaoNome,
            (SELECT COUNT(*) FROM aplicacoes a WHERE a.tratamentoId = t.id) AS aplicadas,
            (SELECT COUNT(*) FROM aplicacoes a WHERE a.tratamentoId = t.id AND a.pago = 0) AS naoPagas,
            (SELECT MAX(a.dataHora) FROM aplicacoes a WHERE a.tratamentoId = t.id) AS ultimaAplicacao
        FROM tratamentos t
        JOIN clientes c ON c.id = t.clienteId
        JOIN injecoes i ON i.id = t.injecaoId
        WHERE t.clienteId = :clienteId
        ORDER BY t.encerrado ASC, t.iniciadoEm DESC
        """
    )
    fun doCliente(clienteId: Long): Flow<List<TratamentoResumo>>

    @Query("SELECT * FROM tratamentos WHERE clienteId = :clienteId AND injecaoId = :injecaoId AND encerrado = 0 LIMIT 1")
    suspend fun ativoDoPar(clienteId: Long, injecaoId: Long): Tratamento?

    @Query("SELECT * FROM tratamentos WHERE id = :id")
    suspend fun porId(id: Long): Tratamento?

    @Insert
    suspend fun inserir(tratamento: Tratamento): Long

    @Update
    suspend fun atualizar(tratamento: Tratamento)
}

@Dao
interface AplicacaoDao {
    @Query(
        """
        SELECT a.*, i.nomeComercial AS injecaoNome, f.nome AS funcionarioNome,
            t.pagamentoAntecipado AS pagamentoAntecipado
        FROM aplicacoes a
        JOIN tratamentos t ON t.id = a.tratamentoId
        JOIN injecoes i ON i.id = t.injecaoId
        JOIN funcionarios f ON f.id = a.funcionarioId
        WHERE t.clienteId = :clienteId
        ORDER BY a.dataHora DESC
        """
    )
    fun historicoDoCliente(clienteId: Long): Flow<List<AplicacaoHistorico>>

    @Query("SELECT COUNT(*) FROM aplicacoes WHERE tratamentoId = :tratamentoId")
    suspend fun contarDoTratamento(tratamentoId: Long): Int

    @Query("UPDATE aplicacoes SET pago = :pago WHERE id = :id")
    suspend fun marcarPago(id: Long, pago: Boolean)

    @Insert
    suspend fun inserir(aplicacao: Aplicacao): Long
}
