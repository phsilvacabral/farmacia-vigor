package com.farmaciavigor.app.ui.telas

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.farmaciavigor.app.FarmaciaApp
import com.farmaciavigor.app.Navegador
import com.farmaciavigor.app.Sessao
import com.farmaciavigor.app.Tela
import com.farmaciavigor.app.dados.AplicacaoHistorico
import com.farmaciavigor.app.dados.TratamentoResumo
import com.farmaciavigor.app.ui.comum.Avatar
import com.farmaciavigor.app.ui.comum.BotaoGrande
import com.farmaciavigor.app.ui.comum.BotaoGrandeContorno
import com.farmaciavigor.app.ui.comum.BotaoIcone
import com.farmaciavigor.app.ui.comum.Cabecalho
import com.farmaciavigor.app.ui.comum.CartaoBase
import com.farmaciavigor.app.ui.comum.DialogoGrande
import com.farmaciavigor.app.ui.comum.ehTablet
import com.farmaciavigor.app.ui.comum.Etiqueta
import com.farmaciavigor.app.ui.comum.LinhaEtiquetas
import com.farmaciavigor.app.ui.comum.LinhaInterruptor
import com.farmaciavigor.app.ui.comum.ParDeBotoes
import com.farmaciavigor.app.ui.tema.AmbarAviso
import com.farmaciavigor.app.ui.tema.VerdeConfirmacao
import com.farmaciavigor.app.util.Formatos
import kotlinx.coroutines.launch

@Composable
fun TelaDetalheCliente(nav: Navegador, clienteId: Long) {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as FarmaciaApp
    val escopo = rememberCoroutineScope()

    val cliente by app.banco.clienteDao().porIdFlow(clienteId).collectAsState(initial = null)
    val tratamentos by app.banco.tratamentoDao().doCliente(clienteId).collectAsState(initial = emptyList())
    val historico by app.banco.aplicacaoDao().historicoDoCliente(clienteId).collectAsState(initial = emptyList())

    var registrarEm by remember { mutableStateOf<TratamentoResumo?>(null) }
    var encerrar by remember { mutableStateOf<TratamentoResumo?>(null) }
    var mostrarEncerrados by remember { mutableStateOf(false) }
    var mostrarHistorico by remember { mutableStateOf(false) }

    // Só os tratamentos em andamento aparecem direto; encerrados e concluídos
    // (ambos têm encerrado = true) ficam recolhidos numa seção expansível.
    val ativos = tratamentos.filter { !it.tratamento.encerrado }
    val encerrados = tratamentos.filter { it.tratamento.encerrado }

    // Tela empilhada (usa "voltar", não o menu), então descobre o tamanho sozinha:
    // no celular o nome do cliente não divide a linha com um botão de texto.
    val ehTablet = ehTablet()

    Column(Modifier.fillMaxSize()) {
        Cabecalho(cliente?.nomeCompleto ?: "Cliente", aoVoltar = { nav.voltar() }) {
            if (ehTablet) {
                BotaoGrandeContorno(
                    "Editar dados",
                    { nav.ir(Tela.FormularioCliente(clienteId)) },
                    icone = Icons.Filled.Edit,
                )
            } else {
                BotaoIcone(
                    Icons.Filled.Edit, "Editar dados",
                    { nav.ir(Tela.FormularioCliente(clienteId)) },
                )
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 48.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            cliente?.let { c ->
                item {
                    CartaoBase {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(c.nomeCompleto, tamanho = 72)
                            Spacer(Modifier.width(20.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Nascimento: ${Formatos.data(c.dataNascimento)} — ${Formatos.idade(c.dataNascimento)} anos",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text("Sexo: ${c.sexo}", style = MaterialTheme.typography.bodyLarge)
                                c.cpf?.let {
                                    Text("CPF: ${Formatos.mascaraCpf(it)}", style = MaterialTheme.typography.bodyLarge)
                                }
                                c.telefone?.let { telefone ->
                                    val digitos = telefone.filter { caractere -> caractere.isDigit() }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            try {
                                                contexto.startActivity(
                                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digitos"))
                                                )
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    contexto,
                                                    "Nenhum aplicativo de telefone encontrado.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        },
                                    ) {
                                        Icon(
                                            Icons.Filled.Phone,
                                            contentDescription = "Ligar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(26.dp),
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Telefone: ${Formatos.mascaraTelefone(digitos)}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                // No celular não cabem título e botão lado a lado: o botão desce e ocupa
                // a largura toda, o que ainda dá um alvo de toque maior.
                if (ehTablet) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 10.dp),
                    ) {
                        Text(
                            "Tratamentos",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                        )
                        BotaoGrande(
                            "Nova aplicação",
                            { nav.ir(Tela.NovaAplicacao(clienteId)) },
                            icone = Icons.Filled.Add,
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 10.dp),
                    ) {
                        Text("Tratamentos", style = MaterialTheme.typography.titleLarge)
                        BotaoGrande(
                            "Nova aplicação",
                            { nav.ir(Tela.NovaAplicacao(clienteId)) },
                            Modifier.fillMaxWidth(),
                            icone = Icons.Filled.Add,
                        )
                    }
                }
            }
            if (ativos.isEmpty()) {
                item {
                    Text(
                        if (tratamentos.isEmpty())
                            "Nenhum tratamento registrado ainda. Toque em \"Nova aplicação\" para começar."
                        else
                            "Nenhum tratamento em andamento no momento.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(ativos, key = { "tratamento-${it.tratamento.id}" }) { resumo ->
                    CartaoTratamentoCliente(
                        resumo,
                        aoRegistrar = { registrarEm = resumo },
                        aoEncerrar = { encerrar = resumo },
                    )
                }
            }

            if (encerrados.isNotEmpty()) {
                item {
                    CabecalhoExpansivel(
                        "Tratamentos encerrados",
                        encerrados.size,
                        mostrarEncerrados,
                    ) { mostrarEncerrados = !mostrarEncerrados }
                }
                if (mostrarEncerrados) {
                    items(encerrados, key = { "encerrado-${it.tratamento.id}" }) { resumo ->
                        CartaoTratamentoCliente(resumo, aoRegistrar = {}, aoEncerrar = {})
                    }
                }
            }

            if (historico.isNotEmpty()) {
                item {
                    CabecalhoExpansivel(
                        "Histórico de aplicações",
                        historico.size,
                        mostrarHistorico,
                    ) { mostrarHistorico = !mostrarHistorico }
                }
                if (mostrarHistorico) {
                    items(historico, key = { "aplicacao-${it.aplicacao.id}" }) { registro ->
                        LinhaHistorico(registro) {
                            escopo.launch { app.banco.aplicacaoDao().marcarPago(registro.aplicacao.id, true) }
                        }
                    }
                }
            }
        }
    }

    registrarEm?.let { resumo ->
        DialogoRegistrarAplicacao(
            resumo = resumo,
            aoFechar = { registrarEm = null },
            aoConfirmar = { paga ->
                escopo.launch {
                    val funcionario = Sessao.funcionario ?: return@launch
                    val concluido = app.repositorio.registrarNoTratamento(resumo.tratamento.id, funcionario.id, paga)
                    Toast.makeText(
                        contexto,
                        if (concluido) "Aplicação registrada — tratamento concluído!" else "Aplicação registrada!",
                        Toast.LENGTH_LONG,
                    ).show()
                    registrarEm = null
                }
            },
        )
    }

    encerrar?.let { resumo ->
        DialogoGrande("Encerrar tratamento", aoFechar = { encerrar = null }) {
            Text(
                "Encerrar o tratamento de ${resumo.injecaoNome} com ${resumo.aplicadas} de " +
                    "${resumo.tratamento.totalDoses} aplicações feitas? O cliente sai da lista de " +
                    "clientes em tratamento, mas o histórico é mantido.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(24.dp))
            ParDeBotoes(
                primario = { m ->
                    BotaoGrande(
                        "Encerrar",
                        {
                            escopo.launch {
                                app.repositorio.encerrarTratamento(resumo.tratamento.id)
                                encerrar = null
                            }
                        },
                        m,
                    )
                },
                secundario = { m -> BotaoGrandeContorno("Cancelar", { encerrar = null }, m) },
            )
        }
    }
}

/** Cabeçalho clicável de uma seção recolhível, com seta que gira ao expandir. */
@Composable
private fun CabecalhoExpansivel(
    titulo: String,
    quantidade: Int,
    expandido: Boolean,
    aoAlternar: () -> Unit,
) {
    val rotacao by animateFloatAsState(if (expandido) 180f else 0f, label = "seta-secao")
    CartaoBase(aoClicar = aoAlternar) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$titulo ($quantidade)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expandido) "Recolher" else "Expandir",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(38.dp)
                    .rotate(rotacao),
            )
        }
    }
}

@Composable
private fun CartaoTratamentoCliente(
    resumo: TratamentoResumo,
    aoRegistrar: () -> Unit,
    aoEncerrar: () -> Unit,
) {
    val tratamento = resumo.tratamento
    CartaoBase {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(resumo.injecaoNome, style = MaterialTheme.typography.titleMedium)
                Text(
                    // Sem aplicação ainda, "iniciado" seria enganoso: o tratamento foi aberto.
                    if (resumo.aguardandoPrimeira) "Aberto em ${Formatos.dataHora(tratamento.iniciadoEm)}"
                    else "Iniciado em ${Formatos.dataHora(tratamento.iniciadoEm)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                resumo.concluido -> Etiqueta("Concluído", VerdeConfirmacao)
                tratamento.encerrado -> Etiqueta("Encerrado", MaterialTheme.colorScheme.secondary)
                resumo.aguardandoPrimeira -> Etiqueta("Aguardando 1ª aplicação", AmbarAviso)
                else -> Etiqueta("Em andamento", MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(14.dp))
        LinearProgressIndicator(
            progress = { (resumo.aplicadas.toFloat() / tratamento.totalDoses).coerceIn(0f, 1f) },
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
        )
        Spacer(Modifier.height(10.dp))
        LinhaEtiquetas {
            Text(
                "${resumo.aplicadas} de ${tratamento.totalDoses} aplicações",
                style = MaterialTheme.typography.titleSmall,
            )
            if (tratamento.pagamentoAntecipado) {
                Etiqueta("Pagamento antecipado", VerdeConfirmacao)
            } else {
                Etiqueta("Paga a cada aplicação", MaterialTheme.colorScheme.secondary)
            }
            if (resumo.naoPagas > 0) {
                Etiqueta("${resumo.naoPagas} não paga(s)", MaterialTheme.colorScheme.error)
            }
        }
        if (!tratamento.encerrado) {
            Spacer(Modifier.height(18.dp))
            ParDeBotoes(
                primario = { m ->
                    BotaoGrande("Registrar aplicação", aoRegistrar, m, icone = Icons.Filled.Vaccines)
                },
                secundario = { m -> BotaoGrandeContorno("Encerrar", aoEncerrar, m) },
            )
        }
    }
}

@Composable
private fun DialogoRegistrarAplicacao(
    resumo: TratamentoResumo,
    aoFechar: () -> Unit,
    aoConfirmar: (Boolean) -> Unit,
) {
    var paga by remember { mutableStateOf(false) }
    DialogoGrande("Registrar aplicação", aoFechar) {
        Text(
            "${resumo.injecaoNome} — aplicação ${resumo.aplicadas + 1} de " +
                "${resumo.tratamento.totalDoses} para ${resumo.clienteNome}.",
            style = MaterialTheme.typography.bodyLarge,
        )
        if (!resumo.tratamento.pagamentoAntecipado) {
            Spacer(Modifier.height(16.dp))
            LinhaInterruptor("Esta aplicação foi paga", paga, { paga = it })
        }
        Sessao.funcionario?.let {
            Spacer(Modifier.height(16.dp))
            Text(
                "Será registrada com a data e o horário de agora, aplicada por ${it.nome}.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))
        ParDeBotoes(
            primario = { m -> BotaoGrande("Registrar", { aoConfirmar(paga) }, m) },
            secundario = { m -> BotaoGrandeContorno("Cancelar", aoFechar, m) },
        )
    }
}

@Composable
private fun LinhaHistorico(registro: AplicacaoHistorico, aoMarcarPaga: () -> Unit) {
    CartaoBase {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(registro.injecaoNome, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(Formatos.dataHora(registro.aplicacao.dataHora), style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Aplicada por ${registro.funcionarioNome}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                registro.pagamentoAntecipado -> Etiqueta("Pago antecipado", VerdeConfirmacao)
                registro.aplicacao.pago -> Etiqueta("Paga", VerdeConfirmacao)
                else -> Column(horizontalAlignment = Alignment.End) {
                    Etiqueta("Não paga", MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(10.dp))
                    BotaoGrandeContorno("Marcar como paga", aoMarcarPaga)
                }
            }
        }
    }
}
