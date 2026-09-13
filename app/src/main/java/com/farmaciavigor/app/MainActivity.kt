package com.farmaciavigor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.farmaciavigor.app.ui.tema.TemaFarmaciaVigor
import com.farmaciavigor.app.ui.telas.TelaDetalheCliente
import com.farmaciavigor.app.ui.telas.TelaFormularioCliente
import com.farmaciavigor.app.ui.telas.TelaFormularioInjecao
import com.farmaciavigor.app.ui.telas.TelaFuncionarios
import com.farmaciavigor.app.ui.telas.TelaNovaAplicacao
import com.farmaciavigor.app.ui.telas.TelaPrincipal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        esconderBarrasDoSistema()
        setContent {
            TemaFarmaciaVigor {
                val densidadeBase = LocalDensity.current
                // Zoom configurável: escala tudo (textos, botões, espaçamentos).
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = densidadeBase.density * Ajustes.zoom,
                        fontScale = densidadeBase.fontScale,
                    )
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        AppRaiz()
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Ao voltar de um diálogo do sistema (câmera, teclado…), reesconde as barras.
        if (hasFocus) esconderBarrasDoSistema()
    }

    /** Modo imersivo: esconde as barras de status e de navegação (tela cheia). */
    private fun esconderBarrasDoSistema() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controlador = WindowInsetsControllerCompat(window, window.decorView)
        controlador.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controlador.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun AppRaiz() {
    // A cada abertura do app (nova Activity), começa do zero na tela de login e
    // descarta qualquer funcionário que tenha ficado em memória de uma sessão anterior.
    val nav = remember {
        Sessao.funcionario = null
        Navegador()
    }
    BackHandler(enabled = nav.podeVoltar) { nav.voltar() }

    val tela = nav.atual
    // Rede de segurança: se perder o funcionário durante o uso, volta para o login.
    LaunchedEffect(tela) {
        if (tela != Tela.Funcionarios && Sessao.funcionario == null) {
            nav.definir(Tela.Funcionarios)
        }
    }

    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        AnimatedContent(
            targetState = tela,
            transitionSpec = {
                val direcao = if (nav.avancando) 1 else -1
                (slideInHorizontally(tween(300)) { largura -> direcao * largura / 6 } + fadeIn(tween(300)))
                    .togetherWith(
                        slideOutHorizontally(tween(300)) { largura -> -direcao * largura / 6 } + fadeOut(tween(300))
                    )
            },
            label = "troca-de-tela",
        ) { telaVisivel ->
            when (telaVisivel) {
                Tela.Funcionarios -> TelaFuncionarios(nav)
                Tela.Principal -> TelaPrincipal(nav)
                is Tela.DetalheCliente -> TelaDetalheCliente(nav, telaVisivel.clienteId)
                is Tela.FormularioCliente -> TelaFormularioCliente(nav, telaVisivel.clienteId)
                is Tela.FormularioInjecao -> TelaFormularioInjecao(nav, telaVisivel.injecaoId)
                is Tela.NovaAplicacao -> TelaNovaAplicacao(nav, telaVisivel.clienteId)
            }
        }
    }
}
