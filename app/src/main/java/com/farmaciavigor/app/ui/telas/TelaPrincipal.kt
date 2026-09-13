package com.farmaciavigor.app.ui.telas

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.farmaciavigor.app.Ajustes
import com.farmaciavigor.app.FarmaciaApp
import com.farmaciavigor.app.Navegador
import com.farmaciavigor.app.Sessao
import com.farmaciavigor.app.Tela
import com.farmaciavigor.app.dados.Cliente
import com.farmaciavigor.app.dados.Injecao
import com.farmaciavigor.app.dados.TratamentoResumo
import com.farmaciavigor.app.ui.comum.Avatar
import com.farmaciavigor.app.ui.comum.BotaoGrande
import com.farmaciavigor.app.ui.comum.BotaoGrandeContorno
import com.farmaciavigor.app.ui.comum.BotaoIcone
import com.farmaciavigor.app.ui.comum.Cabecalho
import com.farmaciavigor.app.ui.comum.CampoBusca
import com.farmaciavigor.app.ui.comum.CartaoBase
import com.farmaciavigor.app.ui.comum.DialogoGrande
import com.farmaciavigor.app.ui.comum.Etiqueta
import com.farmaciavigor.app.ui.comum.LinhaEtiquetas
import com.farmaciavigor.app.ui.comum.LogotipoVigor
import com.farmaciavigor.app.ui.comum.MensagemVazia
import com.farmaciavigor.app.ui.comum.ParDeBotoes
import com.farmaciavigor.app.ui.comum.ehTablet
import com.farmaciavigor.app.ui.tema.AmbarAviso
import com.farmaciavigor.app.ui.tema.VerdeConfirmacao
import com.farmaciavigor.app.util.Backup
import com.farmaciavigor.app.util.Formatos
import com.farmaciavigor.app.util.Fotos
import kotlin.math.roundToInt

enum class Secao(val titulo: String, val icone: ImageVector) {
    TRATAMENTOS("Em tratamento", Icons.Filled.Vaccines),
    CLIENTES("Clientes", Icons.Filled.Groups),
    INJECOES("Injeções", Icons.Filled.Medication),
    AJUSTES("Ajustes", Icons.Filled.Settings),
}

@Composable
fun TelaPrincipal(nav: Navegador) {
    val secao = Secao.entries[nav.abaHome.coerceIn(0, Secao.entries.lastIndex)]
    // Acima de 600dp mantém a barra lateral fixa (ver ehTablet()).
    val ehTablet = ehTablet()

    if (ehTablet) {
        // TABLET (dispositivo principal) — layout de barra lateral fixa, inalterado.
        Row(Modifier.fillMaxSize()) {
            BarraLateral(
                secao,
                { nav.abaHome = it.ordinal },
                nav,
                Modifier.fillMaxHeight().width(190.dp),
            )
            VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ConteudoPrincipal(secao, nav, aoAbrirMenu = null, modifier = Modifier.weight(1f).fillMaxHeight())
        }
    } else {
        // CELULAR — barra lateral vira gaveta, aberta pelo botão de menu no cabeçalho.
        val estadoGaveta = rememberDrawerState(DrawerValue.Closed)
        val escopo = rememberCoroutineScope()
        ModalNavigationDrawer(
            drawerState = estadoGaveta,
            drawerContent = {
                ModalDrawerSheet(Modifier.width(240.dp)) {
                    BarraLateral(
                        secao,
                        { s -> nav.abaHome = s.ordinal; escopo.launch { estadoGaveta.close() } },
                        nav,
                        Modifier.fillMaxSize(),
                    )
                }
            },
        ) {
            ConteudoPrincipal(
                secao,
                nav,
                aoAbrirMenu = { escopo.launch { estadoGaveta.open() } },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ConteudoPrincipal(
    secao: Secao,
    nav: Navegador,
    aoAbrirMenu: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Crossfade(targetState = secao, animationSpec = tween(250), label = "abas") { abaVisivel ->
            when (abaVisivel) {
                Secao.TRATAMENTOS -> AbaTratamentos(nav, aoAbrirMenu)
                Secao.CLIENTES -> AbaClientes(nav, aoAbrirMenu)
                Secao.INJECOES -> AbaInjecoes(nav, aoAbrirMenu)
                Secao.AJUSTES -> AbaAjustes(nav, aoAbrirMenu)
            }
        }
        if (secao != Secao.AJUSTES) {
            ExtendedFloatingActionButton(
                onClick = { nav.ir(Tela.NovaAplicacao(null)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.Vaccines, contentDescription = null, modifier = Modifier.size(30.dp)) },
                text = { Text("Nova aplicação", style = MaterialTheme.typography.labelLarge) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(28.dp),
            )
        }
    }
}

@Composable
private fun BarraLateral(
    secaoAtiva: Secao,
    aoTrocar: (Secao) -> Unit,
    nav: Navegador,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp, vertical = 20.dp),
        ) {
            LogotipoVigor(tamanho = 54)
            Spacer(Modifier.height(8.dp))
            Text("Farmácia Vigor", style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(22.dp))
            Secao.entries.forEach { secao ->
                ItemBarraLateral(secao, secao == secaoAtiva) { aoTrocar(secao) }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.weight(1f))
            Sessao.funcionario?.let { funcionario ->
                Avatar(funcionario.nome, tamanho = 52)
                Spacer(Modifier.height(8.dp))
                Text(
                    Formatos.primeiroNome(funcionario.nome),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                TextButton(onClick = {
                    Sessao.funcionario = null
                    nav.definir(Tela.Funcionarios)
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Trocar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun ItemBarraLateral(secao: Secao, ativa: Boolean, aoClicar: () -> Unit) {
    Surface(
        onClick = aoClicar,
        shape = RoundedCornerShape(16.dp),
        color = if (ativa) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (ativa) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
        ) {
            Icon(secao.icone, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(6.dp))
            Text(secao.titulo, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}

// ---------------------------------------------------------------- Tratamentos

@Composable
private fun AbaTratamentos(nav: Navegador, aoAbrirMenu: (() -> Unit)?) {
    val app = LocalContext.current.applicationContext as FarmaciaApp
    val resumos by app.banco.tratamentoDao().ativos().collectAsState(initial = null)

    Column(Modifier.fillMaxSize()) {
        Cabecalho("Clientes em tratamento", aoAbrirMenu = aoAbrirMenu)
        val lista = resumos
        when {
            lista == null -> Unit
            lista.isEmpty() -> MensagemVazia(
                Icons.Filled.Vaccines,
                "Nenhum tratamento em andamento",
                "Toque em \"Nova aplicação\" para registrar a primeira injeção de um cliente.",
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 130.dp),
            ) {
                items(lista, key = { it.tratamento.id }) { resumo ->
                    CartaoTratamentoAtivo(resumo) { nav.ir(Tela.DetalheCliente(resumo.tratamento.clienteId)) }
                }
            }
        }
    }
}

@Composable
private fun CartaoTratamentoAtivo(resumo: TratamentoResumo, aoClicar: () -> Unit) {
    CartaoBase(aoClicar = aoClicar) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(resumo.clienteNome, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(2.dp))
                Text(
                    resumo.injecaoNome,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { (resumo.aplicadas.toFloat() / resumo.tratamento.totalDoses).coerceIn(0f, 1f) },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${resumo.aplicadas} de ${resumo.tratamento.totalDoses} aplicações",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    resumo.ultimaAplicacao?.let {
                        Text(
                            "Última: ${Formatos.dataHora(it)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                LinhaEtiquetas {
                    if (resumo.aguardandoPrimeira) {
                        Etiqueta("Aguardando 1ª aplicação", AmbarAviso)
                    }
                    if (resumo.tratamento.pagamentoAntecipado) {
                        Etiqueta("Pagamento antecipado", VerdeConfirmacao)
                    } else {
                        Etiqueta("Paga a cada aplicação", MaterialTheme.colorScheme.secondary)
                    }
                    if (resumo.naoPagas > 0) {
                        Etiqueta("${resumo.naoPagas} não paga(s)", MaterialTheme.colorScheme.error)
                    }
                }
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

// ------------------------------------------------------------------- Clientes

@Composable
private fun AbaClientes(nav: Navegador, aoAbrirMenu: (() -> Unit)?) {
    val app = LocalContext.current.applicationContext as FarmaciaApp
    val clientes by app.banco.clienteDao().todos().collectAsState(initial = null)
    var busca by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        // No celular (aoAbrirMenu != null) o espaço é curto: botão de ação vira só o ícone "+".
        Cabecalho("Clientes", aoAbrirMenu = aoAbrirMenu) {
            if (aoAbrirMenu != null) {
                BotaoIcone(
                    Icons.Filled.Add, "Novo cliente", { nav.ir(Tela.FormularioCliente(null)) },
                    cor = MaterialTheme.colorScheme.primary, corConteudo = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                BotaoGrande("Novo cliente", { nav.ir(Tela.FormularioCliente(null)) }, icone = Icons.Filled.Add)
            }
        }
        CampoBusca(
            busca,
            { busca = it },
            Modifier.padding(horizontal = 24.dp),
            dica = "Buscar por nome ou CPF…",
        )
        Spacer(Modifier.height(16.dp))
        val lista = clientes
        val buscaTexto = Formatos.paraBusca(busca)
        val buscaDigitos = busca.filter { it.isDigit() }
        val filtrados = lista.orEmpty().filter { cliente ->
            busca.isBlank() ||
                Formatos.paraBusca(cliente.nomeCompleto).contains(buscaTexto) ||
                (buscaDigitos.isNotEmpty() &&
                    cliente.cpf?.filter { it.isDigit() }?.contains(buscaDigitos) == true)
        }
        when {
            lista == null -> Unit
            lista.isEmpty() -> MensagemVazia(
                Icons.Filled.Groups,
                "Nenhum cliente cadastrado",
                "Toque em \"Novo cliente\" para cadastrar o primeiro.",
            )
            filtrados.isEmpty() -> MensagemVazia(
                Icons.Filled.Search,
                "Nada encontrado",
                "Nenhum cliente com esse nome ou CPF.",
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 130.dp),
            ) {
                items(filtrados, key = { it.id }) { cliente ->
                    CartaoCliente(cliente) { nav.ir(Tela.DetalheCliente(cliente.id)) }
                }
            }
        }
    }
}

@Composable
private fun CartaoCliente(cliente: Cliente, aoClicar: () -> Unit) {
    CartaoBase(aoClicar = aoClicar) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(cliente.nomeCompleto, tamanho = 60)
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(cliente.nomeCompleto, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    listOfNotNull(
                        "${Formatos.idade(cliente.dataNascimento)} anos",
                        cliente.sexo,
                        cliente.telefone?.let { Formatos.mascaraTelefone(it) },
                    ).joinToString("  •  "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

// ------------------------------------------------------------------- Injeções

@Composable
private fun AbaInjecoes(nav: Navegador, aoAbrirMenu: (() -> Unit)?) {
    val app = LocalContext.current.applicationContext as FarmaciaApp
    val injecoes by app.banco.injecaoDao().todas().collectAsState(initial = null)

    Column(Modifier.fillMaxSize()) {
        Cabecalho("Injeções", aoAbrirMenu = aoAbrirMenu) {
            if (aoAbrirMenu != null) {
                BotaoIcone(
                    Icons.Filled.Add, "Nova injeção", { nav.ir(Tela.FormularioInjecao(null)) },
                    cor = MaterialTheme.colorScheme.primary, corConteudo = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                BotaoGrande("Nova injeção", { nav.ir(Tela.FormularioInjecao(null)) }, icone = Icons.Filled.Add)
            }
        }
        val lista = injecoes
        when {
            lista == null -> Unit
            lista.isEmpty() -> MensagemVazia(
                Icons.Filled.Medication,
                "Nenhuma injeção cadastrada",
                "Cadastre os medicamentos que a farmácia aplica.",
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 130.dp),
            ) {
                items(lista, key = { it.id }) { injecao ->
                    CartaoInjecao(injecao) { nav.ir(Tela.FormularioInjecao(injecao.id)) }
                }
            }
        }
    }
}

@Composable
private fun CartaoInjecao(injecao: Injecao, aoClicar: () -> Unit) {
    val contexto = LocalContext.current
    CartaoBase(aoClicar = aoClicar) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(84.dp),
            ) {
                if (injecao.fotoArquivo != null) {
                    AsyncImage(
                        model = Fotos.arquivo(contexto, injecao.fotoArquivo),
                        contentDescription = "Foto da caixa",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(38.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(injecao.nomeComercial, style = MaterialTheme.typography.titleMedium)
                val detalhes = listOfNotNull(
                    injecao.laboratorio,
                    Formatos.composicaoLegivel(injecao.composicao),
                ).joinToString("  •  ")
                if (detalhes.isNotBlank()) {
                    Text(
                        detalhes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Etiqueta("${injecao.qtdAplicacoes} aplicações por tratamento", MaterialTheme.colorScheme.primary)
            }
            Icon(
                Icons.Filled.Edit,
                contentDescription = "Editar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

// -------------------------------------------------------------------- Ajustes

@Composable
private fun AbaAjustes(nav: Navegador, aoAbrirMenu: (() -> Unit)?) {
    Column(Modifier.fillMaxSize()) {
        Cabecalho("Ajustes", aoAbrirMenu = aoAbrirMenu)
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            CartaoBase {
                Text("Zoom da tela", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Deixa letras, botões e listas maiores ou menores em todo o aplicativo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(18.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    BotaoIcone(Icons.Filled.ZoomOut, "Diminuir zoom", { Ajustes.definirZoom(Ajustes.zoom - 0.1f) })
                    Slider(
                        value = Ajustes.zoom,
                        onValueChange = { Ajustes.definirZoom(it) },
                        valueRange = Ajustes.ZOOM_MINIMO..Ajustes.ZOOM_MAXIMO,
                        steps = 6,
                        modifier = Modifier.weight(1f),
                    )
                    BotaoIcone(Icons.Filled.ZoomIn, "Aumentar zoom", { Ajustes.definirZoom(Ajustes.zoom + 0.1f) })
                    Text("${(Ajustes.zoom * 100).roundToInt()}%", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Exemplo: Maria Souza — 3 de 10 aplicações",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(18.dp),
                    )
                }
            }
            Sessao.funcionario?.let { funcionario ->
                CartaoBase {
                    Text("Atendente", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(funcionario.nome, tamanho = 56)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(funcionario.nome, style = MaterialTheme.typography.titleMedium)
                            Text(
                                funcionario.sexo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    BotaoGrandeContorno(
                        "Trocar funcionário",
                        {
                            Sessao.funcionario = null
                            nav.definir(Tela.Funcionarios)
                        },
                        icone = Icons.AutoMirrored.Filled.Logout,
                    )
                }
            }
            SecaoBackup()
            CartaoBase {
                Text("Sobre", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Farmácia Vigor — controle de injeções aplicadas em clientes em tratamento. Versão 1.0.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SecaoBackup() {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as FarmaciaApp
    val escopo = rememberCoroutineScope()
    var ocupado by remember { mutableStateOf(false) }
    var confirmarRestauracao by remember { mutableStateOf<Uri?>(null) }

    val exportar = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { destino ->
        if (destino != null) {
            ocupado = true
            escopo.launch {
                val resultado = Backup.exportar(contexto, app.banco, destino)
                ocupado = false
                Toast.makeText(
                    contexto,
                    resultado.fold(
                        { "Backup salvo com sucesso!" },
                        { "Falha ao salvar o backup: ${it.message}" },
                    ),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }
    val escolherParaRestaurar = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { origem ->
        if (origem != null) confirmarRestauracao = origem
    }

    CartaoBase {
        Text("Backup dos dados", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Salva clientes, injeções, tratamentos, histórico e fotos em um único arquivo. " +
                "Guarde no seu Google Drive para não perder nada ao trocar de tablet ou reinstalar o app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(18.dp))
        ParDeBotoes(
            primario = { m ->
                BotaoGrande(
                    "Exportar backup",
                    { if (!ocupado) exportar.launch(Backup.nomeSugerido()) },
                    m,
                    habilitado = !ocupado,
                    icone = Icons.Filled.Upload,
                )
            },
            secundario = { m ->
                BotaoGrandeContorno(
                    "Restaurar backup",
                    { if (!ocupado) escolherParaRestaurar.launch(arrayOf("application/zip", "application/octet-stream", "*/*")) },
                    m,
                    habilitado = !ocupado,
                    icone = Icons.Filled.Download,
                )
            },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "O backup automático do Android também guarda os dados na sua conta Google (sem as fotos).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    confirmarRestauracao?.let { origem ->
        DialogoGrande("Restaurar backup", aoFechar = { confirmarRestauracao = null }) {
            Text(
                "Isto vai substituir TODOS os dados atuais (clientes, injeções, tratamentos, histórico e fotos) " +
                    "pelo conteúdo do backup escolhido. Não dá para desfazer. Deseja continuar?",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BotaoGrandeContorno("Cancelar", { confirmarRestauracao = null }, Modifier.weight(1f))
                BotaoGrande(
                    "Restaurar",
                    {
                        confirmarRestauracao = null
                        ocupado = true
                        escopo.launch {
                            val resultado = Backup.importar(contexto, origem)
                            resultado.fold(
                                onSuccess = {
                                    Toast.makeText(contexto, "Backup restaurado! Reiniciando…", Toast.LENGTH_LONG).show()
                                    Backup.reiniciarApp(contexto)
                                },
                                onFailure = {
                                    ocupado = false
                                    Toast.makeText(
                                        contexto,
                                        "Falha ao restaurar: ${it.message}",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                },
                            )
                        }
                    },
                    Modifier.weight(1f),
                )
            }
        }
    }
}
