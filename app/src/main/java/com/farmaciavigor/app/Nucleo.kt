package com.farmaciavigor.app

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.farmaciavigor.app.dados.Funcionario

/** Funcionário escolhido na tela inicial; usado para registrar quem aplicou. */
object Sessao {
    var funcionario by mutableStateOf<Funcionario?>(null)
}

/** Preferências do app, hoje apenas o zoom da tela. */
object Ajustes {
    const val ZOOM_MINIMO = 0.8f
    const val ZOOM_MAXIMO = 1.5f

    private var prefs: SharedPreferences? = null

    var zoom by mutableFloatStateOf(1f)
        private set

    fun iniciar(contexto: Context) {
        prefs = contexto.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        zoom = prefs?.getFloat("zoom", 1f) ?: 1f
    }

    fun definirZoom(valor: Float) {
        zoom = valor.coerceIn(ZOOM_MINIMO, ZOOM_MAXIMO)
        prefs?.edit()?.putFloat("zoom", zoom)?.apply()
    }
}

sealed interface Tela {
    data object Funcionarios : Tela
    data object Principal : Tela
    data class DetalheCliente(val clienteId: Long) : Tela
    data class FormularioCliente(val clienteId: Long?) : Tela
    data class FormularioInjecao(val injecaoId: Long?) : Tela
    data class NovaAplicacao(val clienteId: Long?) : Tela
}

class Navegador {
    val pilha = mutableStateListOf<Tela>(Tela.Funcionarios)

    val atual: Tela get() = pilha.last()
    val podeVoltar: Boolean get() = pilha.size > 1

    /** Direção da última navegação, usada pela animação de troca de telas. */
    var avancando by mutableStateOf(true)
        private set

    /**
     * Aba selecionada na tela principal (índice de Secao). Fica aqui, fora da
     * TelaPrincipal, para sobreviver quando abrimos um formulário e voltamos —
     * assim editar uma injeção retorna para a aba de Injeções, não para Tratamentos.
     */
    var abaHome by mutableIntStateOf(0)

    fun ir(tela: Tela) {
        avancando = true
        pilha.add(tela)
    }

    fun voltar() {
        if (podeVoltar) {
            avancando = false
            pilha.removeAt(pilha.lastIndex)
        }
    }

    fun definir(vararg telas: Tela) {
        avancando = true
        pilha.clear()
        pilha.addAll(telas)
    }
}
