package com.farmaciavigor.app.ui.telas

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.farmaciavigor.app.FarmaciaApp
import com.farmaciavigor.app.Navegador
import com.farmaciavigor.app.Sessao
import com.farmaciavigor.app.Tela
import com.farmaciavigor.app.dados.Funcionario
import com.farmaciavigor.app.ui.comum.Avatar
import com.farmaciavigor.app.ui.comum.BotaoGrande
import com.farmaciavigor.app.ui.comum.BotaoGrandeContorno
import com.farmaciavigor.app.ui.comum.CampoGrande
import com.farmaciavigor.app.ui.comum.CartaoBase
import com.farmaciavigor.app.ui.comum.ControleZoom
import com.farmaciavigor.app.ui.comum.DialogoGrande
import com.farmaciavigor.app.ui.comum.LogotipoVigor
import com.farmaciavigor.app.ui.comum.SeletorOpcoes
import com.farmaciavigor.app.util.Formatos
import kotlinx.coroutines.launch

/** "Login": mostra os funcionários cadastrados; tocar em um deles entra no app. */
@Composable
fun TelaFuncionarios(nav: Navegador) {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as FarmaciaApp
    val escopo = rememberCoroutineScope()
    val funcionarios by app.banco.funcionarioDao().todos().collectAsState(initial = emptyList())
    var mostrarCadastro by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            ControleZoom()
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            LogotipoVigor()
            Spacer(Modifier.height(14.dp))
            Text("Farmácia Vigor", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Controle de injeções e tratamentos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(30.dp))
            Text("Quem está atendendo?", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(22.dp))
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 240.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(start = 40.dp, end = 40.dp, bottom = 48.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(funcionarios, key = { it.id }) { funcionario ->
                CartaoFuncionario(funcionario) {
                    Sessao.funcionario = funcionario
                    nav.definir(Tela.Principal)
                }
            }
            item {
                CartaoNovoFuncionario { mostrarCadastro = true }
            }
        }
    }

    if (mostrarCadastro) {
        DialogoNovoFuncionario(
            aoFechar = { mostrarCadastro = false },
            aoSalvar = { funcionario ->
                escopo.launch {
                    app.banco.funcionarioDao().inserir(funcionario)
                    mostrarCadastro = false
                    Toast.makeText(contexto, "Funcionário cadastrado!", Toast.LENGTH_SHORT).show()
                }
            },
        )
    }
}

@Composable
private fun CartaoFuncionario(funcionario: Funcionario, aoClicar: () -> Unit) {
    CartaoBase(aoClicar = aoClicar) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Avatar(funcionario.nome, tamanho = 80)
            Spacer(Modifier.height(14.dp))
            Text(
                funcionario.nome.trim().split(" ").first(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                funcionario.sexo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CartaoNovoFuncionario(aoClicar: () -> Unit) {
    Surface(
        onClick = aoClicar,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Novo funcionário",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DialogoNovoFuncionario(aoFechar: () -> Unit, aoSalvar: (Funcionario) -> Unit) {
    var nome by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf<String?>(null) }
    var erros by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    DialogoGrande("Novo funcionário", aoFechar) {
        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            CampoGrande(
                nome,
                { nome = Formatos.capitalizarPalavras(it) },
                "Nome",
                obrigatorio = true,
                erro = erros["nome"],
            )
            SeletorOpcoes(
                "Sexo",
                listOf("Masculino", "Feminino"),
                sexo,
                { sexo = it },
                obrigatorio = true,
                erro = erros["sexo"],
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BotaoGrandeContorno("Cancelar", aoFechar, Modifier.weight(1f))
                BotaoGrande(
                    "Salvar",
                    {
                        val novosErros = buildMap {
                            if (nome.isBlank()) put("nome", "Informe o nome")
                            if (sexo == null) put("sexo", "Escolha o sexo")
                        }
                        erros = novosErros
                        if (novosErros.isEmpty()) {
                            aoSalvar(Funcionario(nome = nome.trim(), sexo = sexo!!))
                        }
                    },
                    Modifier.weight(1f),
                )
            }
        }
    }
}
