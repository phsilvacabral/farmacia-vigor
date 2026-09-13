package com.farmaciavigor.app.dados

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Funcionario::class,
        Cliente::class,
        Injecao::class,
        Tratamento::class,
        Aplicacao::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class BancoDeDados : RoomDatabase() {
    abstract fun funcionarioDao(): FuncionarioDao
    abstract fun clienteDao(): ClienteDao
    abstract fun injecaoDao(): InjecaoDao
    abstract fun tratamentoDao(): TratamentoDao
    abstract fun aplicacaoDao(): AplicacaoDao

    companion object {
        fun criar(contexto: Context): BancoDeDados =
            Room.databaseBuilder(contexto, BancoDeDados::class.java, "farmacia_vigor.db")
                .build()
    }
}
