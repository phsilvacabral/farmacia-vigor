package com.farmaciavigor.app

import android.app.Application
import com.farmaciavigor.app.dados.BancoDeDados
import com.farmaciavigor.app.dados.Repositorio

class FarmaciaApp : Application() {

    val banco: BancoDeDados by lazy { BancoDeDados.criar(this) }
    val repositorio: Repositorio by lazy { Repositorio(banco) }

    override fun onCreate() {
        super.onCreate()
        Ajustes.iniciar(this)
    }
}
