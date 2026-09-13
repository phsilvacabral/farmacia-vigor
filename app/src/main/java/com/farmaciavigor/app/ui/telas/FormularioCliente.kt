package com.farmaciavigor.app.ui.telas

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.farmaciavigor.app.dados.Cliente
import com.farmaciavigor.app.ui.comum.BotaoGrande
import com.farmaciavigor.app.ui.comum.BotaoGrandeContorno
import com.farmaciavigor.app.ui.comum.Cabecalho
import com.farmaciavigor.app.ui.comum.CampoData
import com.farmaciavigor.app.ui.comum.CampoGrande
import com.farmaciavigor.app.ui.comum.DialogoGrande
import com.farmaciavigor.app.ui.comum.SeletorOpcoes
import com.farmaciavigor.app.ui.comum.TransformacaoMascara
import com.farmaciavigor.app.util.Formatos
import kotlinx.coroutines.launch

@Composable
fun TelaFormularioCliente(nav: Navegador, clienteId: Long?) {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as FarmaciaApp
    val escopo = rememberCoroutineScope()

    var original by remember { mutableStateOf<Cliente?>(null) }
    var pronto by remember { mutableStateOf(clienteId == null) }
    val clientes by app.banco.clienteDao().todos().collectAsState(initial = emptyList())
    LaunchedEffect(clienteId) {
        if (clienteId != null) {
            original = app.banco.clienteDao().porId(clienteId)
            pronto = true
        }
    }

    Column(Modifier.fillMaxSize()) {
        Cabecalho(
            if (clienteId == null) "Novo cliente" else "Editar cliente",
            aoVoltar = { nav.voltar() },
        )
        if (pronto) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 840.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .imePadding(),
                ) {
                    FormularioClienteConteudo(
                        original = original,
                        textoSalvar = if (clienteId == null) "Cadastrar cliente" else "Salvar alterações",
                        aoCancelar = null,
                        outrosClientes = clientes,
                    ) { cliente ->
                        escopo.launch {
                            if (cliente.id == 0L) {
                                app.banco.clienteDao().inserir(cliente)
                            } else {
                                app.banco.clienteDao().atualizar(cliente)
                            }
                            Toast.makeText(contexto, "Cliente salvo!", Toast.LENGTH_SHORT).show()
                            nav.voltar()
                        }
                    }
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

/** Cadastro rápido usado dentro do fluxo de nova aplicação. */
@Composable
fun DialogoNovoCliente(aoFechar: () -> Unit, aoCriado: (Cliente) -> Unit) {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as FarmaciaApp
    val escopo = rememberCoroutineScope()
    val clientes by app.banco.clienteDao().todos().collectAsState(initial = emptyList())

    DialogoGrande("Novo cliente", aoFechar) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            FormularioClienteConteudo(
                original = null,
                textoSalvar = "Cadastrar cliente",
                aoCancelar = aoFechar,
                outrosClientes = clientes,
            ) { cliente ->
                escopo.launch {
                    val id = app.banco.clienteDao().inserir(cliente)
                    Toast.makeText(contexto, "Cliente cadastrado!", Toast.LENGTH_SHORT).show()
                    aoCriado(cliente.copy(id = id))
                }
            }
        }
    }
}

@Composable
fun FormularioClienteConteudo(
    original: Cliente?,
    textoSalvar: String,
    aoCancelar: (() -> Unit)?,
    outrosClientes: List<Cliente> = emptyList(),
    aoSalvar: (Cliente) -> Unit,
) {
    var nome by remember(original) { mutableStateOf(original?.nomeCompleto ?: "") }
    // CPF e telefone guardam apenas dígitos; a pontuação é exibida por máscara visual,
    // o que mantém o cursor sempre na posição certa.
    var cpf by remember(original) { mutableStateOf(original?.cpf?.filter { it.isDigit() } ?: "") }
    // Nascimento também guarda só dígitos (ddMMyyyy) — as barras vêm da máscara visual.
    var nascimento by remember(original) {
        mutableStateOf(original?.dataNascimento?.let { Formatos.millisParaDigitosData(it) } ?: "")
    }
    var sexo by remember(original) { mutableStateOf<String?>(original?.sexo) }
    var telefone by remember(original) { mutableStateOf(original?.telefone?.filter { it.isDigit() } ?: "") }
    var erros by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val mascaraCpf = remember { TransformacaoMascara(Formatos::mascaraCpf) }
    val mascaraTelefone = remember { TransformacaoMascara(Formatos::mascaraTelefone) }

    // Duplicidade é conferida contra os outros cadastros (ignorando o próprio, ao editar).
    val outros = outrosClientes.filter { it.id != (original?.id ?: -1L) }
    val cpfDuplicado = cpf.length == 11 && Formatos.cpfValido(cpf) &&
        outros.any { it.cpf?.filter { c -> c.isDigit() } == cpf }
    val nomeDuplicado = nome.isNotBlank() &&
        outros.any { Formatos.paraBusca(it.nomeCompleto) == Formatos.paraBusca(nome) }
    val erroCpfAoVivo = when {
        cpf.isBlank() -> null
        cpf.length == 11 && !Formatos.cpfValido(cpf) -> "CPF inválido — confira os números"
        cpfDuplicado -> "Já existe um cliente com este CPF"
        else -> null
    }
    // Só avisa do que já dá para julgar enquanto digita (dia 45, mês 13, data no futuro).
    val erroNascimentoAoVivo = Formatos.erroData(nascimento)
    val nascimentoMillis = Formatos.dataParaMillis(nascimento)

    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        CampoGrande(
            nome,
            { nome = Formatos.capitalizarPalavras(it) },
            "Nome completo",
            obrigatorio = true,
            erro = erros["nome"],
            aviso = if (nomeDuplicado) "Já existe um cliente com este nome — confira se não é repetido" else null,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            CampoData(
                nascimento,
                { nascimento = it },
                "Data de nascimento",
                Modifier.weight(1f),
                obrigatorio = true,
                erro = erros["nascimento"] ?: erroNascimentoAoVivo,
            )
            CampoGrande(
                cpf,
                { novo -> cpf = novo.filter { it.isDigit() }.take(11) },
                "CPF",
                Modifier.weight(1f),
                erro = erros["cpf"] ?: erroCpfAoVivo,
                ajuda = "Opcional",
                tipoTeclado = KeyboardType.Number,
                transformacao = mascaraCpf,
            )
        }
        SeletorOpcoes(
            "Sexo",
            listOf("Masculino", "Feminino"),
            sexo,
            { sexo = it },
            obrigatorio = true,
            erro = erros["sexo"],
        )
        CampoGrande(
            telefone,
            { novo -> telefone = novo.filter { it.isDigit() }.take(11) },
            "Telefone",
            ajuda = "Opcional",
            tipoTeclado = KeyboardType.Phone,
            transformacao = mascaraTelefone,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (aoCancelar != null) {
                BotaoGrandeContorno("Cancelar", aoCancelar, Modifier.weight(1f))
            }
            BotaoGrande(
                textoSalvar,
                {
                    val novosErros = buildMap {
                        if (nome.isBlank()) put("nome", "Informe o nome completo")
                        if (nascimentoMillis == null) {
                            put(
                                "nascimento",
                                when {
                                    nascimento.isBlank() -> "Informe a data de nascimento"
                                    else -> erroNascimentoAoVivo ?: "Data incompleta — use dd/mm/aaaa"
                                },
                            )
                        }
                        if (sexo == null) put("sexo", "Escolha o sexo")
                        if (cpf.isNotBlank() && !Formatos.cpfValido(cpf)) {
                            put("cpf", "CPF inválido — confira os números")
                        } else if (cpfDuplicado) {
                            put("cpf", "Já existe um cliente com este CPF")
                        }
                    }
                    erros = novosErros
                    if (novosErros.isEmpty()) {
                        aoSalvar(
                            Cliente(
                                id = original?.id ?: 0L,
                                nomeCompleto = nome.trim(),
                                cpf = cpf.trim().ifBlank { null },
                                dataNascimento = nascimentoMillis!!,
                                sexo = sexo!!,
                                telefone = telefone.trim().ifBlank { null },
                            )
                        )
                    }
                },
                Modifier.weight(1f),
            )
        }
    }
}
