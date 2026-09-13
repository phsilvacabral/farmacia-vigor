package com.farmaciavigor.app.ui.telas

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.farmaciavigor.app.FarmaciaApp
import com.farmaciavigor.app.Navegador
import com.farmaciavigor.app.Sessao
import com.farmaciavigor.app.Tela
import com.farmaciavigor.app.dados.Cliente
import com.farmaciavigor.app.dados.Injecao
import com.farmaciavigor.app.dados.Tratamento
import com.farmaciavigor.app.ui.comum.BotaoGrande
import com.farmaciavigor.app.ui.comum.BotaoGrandeContorno
import com.farmaciavigor.app.ui.comum.Cabecalho
import com.farmaciavigor.app.ui.comum.CampoGrande
import com.farmaciavigor.app.ui.comum.CartaoBase
import com.farmaciavigor.app.ui.comum.DialogoSelecao
import com.farmaciavigor.app.ui.comum.LinhaInterruptor
import com.farmaciavigor.app.ui.comum.MensagemErro
import com.farmaciavigor.app.ui.comum.RotuloCampo
import com.farmaciavigor.app.ui.comum.SeletorOpcoes
import com.farmaciavigor.app.util.Formatos
import kotlinx.coroutines.launch

private const val PAGOU_TUDO = "Pagou tudo antecipado"
private const val PAGA_CADA = "Paga a cada aplicação"

@Composable
fun TelaNovaAplicacao(nav: Navegador, clienteIdInicial: Long?) {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as FarmaciaApp
    val escopo = rememberCoroutineScope()
    val funcionario = Sessao.funcionario

    val clientes by app.banco.clienteDao().todos().collectAsState(initial = emptyList())
    val injecoes by app.banco.injecaoDao().todas().collectAsState(initial = emptyList())

    var cliente by remember { mutableStateOf<Cliente?>(null) }
    var injecao by remember { mutableStateOf<Injecao?>(null) }
    var doses by remember { mutableStateOf("") }
    var pagamento by remember { mutableStateOf<String?>(null) }
    var aplicacaoPaga by remember { mutableStateOf(false) }
    var erros by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var salvando by remember { mutableStateOf(false) }

    var tratamentoExistente by remember { mutableStateOf<Tratamento?>(null) }
    var aplicadasExistente by remember { mutableIntStateOf(0) }

    var escolherCliente by remember { mutableStateOf(false) }
    var novoCliente by remember { mutableStateOf(false) }
    var escolherInjecao by remember { mutableStateOf(false) }
    var novaInjecao by remember { mutableStateOf(false) }

    LaunchedEffect(clienteIdInicial) {
        if (clienteIdInicial != null) {
            cliente = app.banco.clienteDao().porId(clienteIdInicial)
        }
    }
    // Se o par cliente+injeção já tem tratamento aberto, a aplicação entra nele.
    LaunchedEffect(cliente?.id, injecao?.id) {
        val c = cliente
        val i = injecao
        tratamentoExistente = if (c != null && i != null) {
            app.banco.tratamentoDao().ativoDoPar(c.id, i.id)
        } else null
        aplicadasExistente = tratamentoExistente
            ?.let { app.banco.aplicacaoDao().contarDoTratamento(it.id) } ?: 0
    }

    Column(Modifier.fillMaxSize()) {
        Cabecalho("Nova aplicação", aoVoltar = { nav.voltar() })
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .imePadding(),
            ) {
                SeletorEntidade(
                    rotulo = "Cliente",
                    valor = cliente?.nomeCompleto,
                    detalhe = cliente?.let { c ->
                        listOfNotNull(
                            "${Formatos.idade(c.dataNascimento)} anos",
                            c.telefone?.let(Formatos::mascaraTelefone),
                        ).joinToString("  •  ")
                    },
                    textoVazio = "Toque para escolher o cliente",
                    erro = erros["cliente"],
                ) { escolherCliente = true }

                SeletorEntidade(
                    rotulo = "Injeção",
                    valor = injecao?.nomeComercial,
                    detalhe = injecao?.laboratorio,
                    textoVazio = "Toque para escolher a injeção",
                    erro = erros["injecao"],
                ) { escolherInjecao = true }

                val existente = tratamentoExistente
                if (existente != null) {
                    CartaoBase {
                        Text(
                            "Tratamento em andamento",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${cliente?.nomeCompleto} já está em tratamento com ${injecao?.nomeComercial} " +
                                "($aplicadasExistente de ${existente.totalDoses} aplicações feitas). " +
                                "Esta será a aplicação ${aplicadasExistente + 1}.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (!existente.pagamentoAntecipado) {
                            Spacer(Modifier.height(14.dp))
                            LinhaInterruptor("Esta aplicação foi paga", aplicacaoPaga, { aplicacaoPaga = it })
                        }
                    }
                } else if (injecao != null) {
                    CampoGrande(
                        doses,
                        { novo -> doses = novo.filter { it.isDigit() }.take(3) },
                        "Quantidade de aplicações do tratamento",
                        obrigatorio = true,
                        erro = erros["doses"],
                        ajuda = "Recomendado: ${injecao?.qtdAplicacoes} (do cadastro da injeção)",
                        tipoTeclado = KeyboardType.Number,
                    )
                    SeletorOpcoes(
                        "Pagamento",
                        listOf(PAGOU_TUDO, PAGA_CADA),
                        pagamento,
                        { pagamento = it },
                        obrigatorio = true,
                        erro = erros["pagamento"],
                    )
                    if (pagamento == PAGA_CADA) {
                        LinhaInterruptor("A aplicação de hoje foi paga", aplicacaoPaga, { aplicacaoPaga = it })
                    }
                }

                if (funcionario != null) {
                    Text(
                        "A aplicação ficará no histórico do cliente com a data e o horário de agora, " +
                            "aplicada por ${funcionario.nome}." +
                            if (existente == null)
                                " Para só deixar o tratamento salvo e aplicar depois, use \"Só abrir o tratamento\"."
                            else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Mesma validação para registrar e para só abrir; devolve true se está tudo certo.
                val validar = {
                    val novosErros = buildMap {
                        if (cliente == null) put("cliente", "Escolha o cliente")
                        if (injecao == null) put("injecao", "Escolha a injeção")
                        if (existente == null && injecao != null) {
                            if ((doses.toIntOrNull() ?: 0) <= 0) {
                                put("doses", "Informe quantas aplicações terá o tratamento")
                            }
                            if (pagamento == null) {
                                put("pagamento", "Escolha a forma de pagamento")
                            }
                        }
                    }
                    erros = novosErros
                    novosErros.isEmpty()
                }

                BotaoGrande(
                    "Registrar aplicação",
                    {
                        val c = cliente
                        val i = injecao
                        if (validar() && c != null && i != null && funcionario != null) {
                            salvando = true
                            escopo.launch {
                                val antecipado = existente?.pagamentoAntecipado ?: (pagamento == PAGOU_TUDO)
                                val resultado = app.repositorio.registrarAplicacao(
                                    clienteId = c.id,
                                    injecaoId = i.id,
                                    totalDoses = existente?.totalDoses ?: doses.toInt(),
                                    pagamentoAntecipado = antecipado,
                                    aplicacaoPaga = if (antecipado) true else aplicacaoPaga,
                                    funcionarioId = funcionario.id,
                                )
                                val mensagem = when {
                                    resultado.tratamentoConcluido -> "Aplicação registrada — tratamento concluído!"
                                    resultado.adicionadaEmTratamentoExistente -> "Aplicação registrada no tratamento em andamento."
                                    else -> "Tratamento iniciado e aplicação registrada!"
                                }
                                Toast.makeText(contexto, mensagem, Toast.LENGTH_LONG).show()
                                nav.definir(Tela.Principal, Tela.DetalheCliente(c.id))
                            }
                        }
                    },
                    Modifier.fillMaxWidth(),
                    habilitado = !salvando,
                    icone = Icons.Filled.Check,
                )

                // Com tratamento já aberto não há o que abrir — só registrar a próxima dose.
                if (existente == null) {
                    BotaoGrandeContorno(
                        "Só abrir o tratamento",
                        {
                            val c = cliente
                            val i = injecao
                            if (validar() && c != null && i != null) {
                                salvando = true
                                escopo.launch {
                                    app.repositorio.abrirTratamento(
                                        clienteId = c.id,
                                        injecaoId = i.id,
                                        totalDoses = doses.toInt(),
                                        pagamentoAntecipado = pagamento == PAGOU_TUDO,
                                    )
                                    Toast.makeText(
                                        contexto,
                                        "Tratamento salvo — aguardando a 1ª aplicação.",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                    nav.definir(Tela.Principal, Tela.DetalheCliente(c.id))
                                }
                            }
                        },
                        Modifier.fillMaxWidth(),
                        habilitado = !salvando,
                        icone = Icons.Filled.Schedule,
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (escolherCliente) {
        DialogoSelecao(
            titulo = "Escolher cliente",
            itens = clientes,
            textoPrincipal = { it.nomeCompleto },
            textoSecundario = { c ->
                listOfNotNull(
                    "${Formatos.idade(c.dataNascimento)} anos",
                    c.telefone?.let(Formatos::mascaraTelefone),
                ).joinToString("  •  ")
            },
            aoEscolher = {
                cliente = it
                escolherCliente = false
            },
            aoCriarNovo = {
                escolherCliente = false
                novoCliente = true
            },
            textoCriarNovo = "Cadastrar novo cliente",
            aoFechar = { escolherCliente = false },
        )
    }
    if (novoCliente) {
        DialogoNovoCliente(
            aoFechar = { novoCliente = false },
            aoCriado = { criado ->
                cliente = criado
                novoCliente = false
            },
        )
    }
    if (escolherInjecao) {
        DialogoSelecao(
            titulo = "Escolher injeção",
            itens = injecoes,
            textoPrincipal = { it.nomeComercial },
            textoSecundario = {
                listOfNotNull(it.laboratorio, "${it.qtdAplicacoes} aplicações por tratamento")
                    .joinToString("  •  ")
            },
            aoEscolher = {
                injecao = it
                doses = it.qtdAplicacoes.toString()
                escolherInjecao = false
            },
            aoCriarNovo = {
                escolherInjecao = false
                novaInjecao = true
            },
            textoCriarNovo = "Cadastrar nova injeção",
            aoFechar = { escolherInjecao = false },
        )
    }
    if (novaInjecao) {
        DialogoNovaInjecao(
            aoFechar = { novaInjecao = false },
            aoCriada = { criada ->
                injecao = criada
                doses = criada.qtdAplicacoes.toString()
                novaInjecao = false
            },
        )
    }
}

@Composable
private fun SeletorEntidade(
    rotulo: String,
    valor: String?,
    detalhe: String?,
    textoVazio: String,
    erro: String?,
    aoClicar: () -> Unit,
) {
    Column {
        RotuloCampo(rotulo, true)
        Surface(
            onClick = aoClicar,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                if (erro != null) 2.dp else 1.dp,
                if (erro != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    if (valor == null) {
                        Text(
                            textoVazio,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(valor, style = MaterialTheme.typography.titleMedium)
                        if (!detalhe.isNullOrBlank()) {
                            Text(
                                detalhe,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    if (valor == null) "Escolher" else "Trocar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    softWrap = false,
                )
            }
        }
        if (erro != null) MensagemErro(erro)
    }
}
