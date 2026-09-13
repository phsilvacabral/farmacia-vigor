package com.farmaciavigor.app.ui.telas

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.farmaciavigor.app.FarmaciaApp
import com.farmaciavigor.app.Navegador
import com.farmaciavigor.app.dados.Injecao
import com.farmaciavigor.app.ui.comum.BotaoGrande
import com.farmaciavigor.app.ui.comum.BotaoGrandeContorno
import com.farmaciavigor.app.ui.comum.BotaoIcone
import com.farmaciavigor.app.ui.comum.Cabecalho
import com.farmaciavigor.app.ui.comum.CampoGrande
import com.farmaciavigor.app.ui.comum.DialogoGrande
import com.farmaciavigor.app.ui.comum.RotuloCampo
import com.farmaciavigor.app.ui.comum.VisualizadorImagem
import com.farmaciavigor.app.util.Formatos
import com.farmaciavigor.app.util.Fotos
import kotlinx.coroutines.launch

@Composable
fun TelaFormularioInjecao(nav: Navegador, injecaoId: Long?) {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as FarmaciaApp
    val escopo = rememberCoroutineScope()

    var original by remember { mutableStateOf<Injecao?>(null) }
    var pronto by remember { mutableStateOf(injecaoId == null) }
    LaunchedEffect(injecaoId) {
        if (injecaoId != null) {
            original = app.banco.injecaoDao().porId(injecaoId)
            pronto = true
        }
    }

    Column(Modifier.fillMaxSize()) {
        Cabecalho(
            if (injecaoId == null) "Nova injeção" else "Editar injeção",
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
                    FormularioInjecaoConteudo(
                        original = original,
                        textoSalvar = if (injecaoId == null) "Cadastrar injeção" else "Salvar alterações",
                        aoCancelar = null,
                    ) { injecao ->
                        escopo.launch {
                            if (injecao.id == 0L) {
                                app.banco.injecaoDao().inserir(injecao)
                            } else {
                                app.banco.injecaoDao().atualizar(injecao)
                            }
                            Toast.makeText(contexto, "Injeção salva!", Toast.LENGTH_SHORT).show()
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
fun DialogoNovaInjecao(aoFechar: () -> Unit, aoCriada: (Injecao) -> Unit) {
    val contexto = LocalContext.current
    val app = contexto.applicationContext as FarmaciaApp
    val escopo = rememberCoroutineScope()

    DialogoGrande("Nova injeção", aoFechar) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            FormularioInjecaoConteudo(
                original = null,
                textoSalvar = "Cadastrar injeção",
                aoCancelar = aoFechar,
            ) { injecao ->
                escopo.launch {
                    val id = app.banco.injecaoDao().inserir(injecao)
                    Toast.makeText(contexto, "Injeção cadastrada!", Toast.LENGTH_SHORT).show()
                    aoCriada(injecao.copy(id = id))
                }
            }
        }
    }
}

@Composable
fun FormularioInjecaoConteudo(
    original: Injecao?,
    textoSalvar: String,
    aoCancelar: (() -> Unit)?,
    aoSalvar: (Injecao) -> Unit,
) {
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()

    var nome by remember(original) { mutableStateOf(original?.nomeComercial ?: "") }
    val compostos = remember(original) {
        mutableStateListOf<Pair<String, String>>().apply {
            addAll(Formatos.desserializarCompostos(original?.composicao))
            if (isEmpty()) add("" to "")
        }
    }
    var laboratorio by remember(original) { mutableStateOf(original?.laboratorio ?: "") }
    var qtd by remember(original) { mutableStateOf(original?.qtdAplicacoes?.toString() ?: "") }
    var foto by remember(original) { mutableStateOf(original?.fotoArquivo) }
    var erros by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var uriCamera by remember { mutableStateOf<Uri?>(null) }
    var visualizandoFoto by remember { mutableStateOf(false) }

    val abrirGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            escopo.launch { Fotos.salvarDeUri(contexto, uri)?.let { foto = it } }
        }
    }
    val abrirCamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { sucesso ->
        val uri = uriCamera
        if (sucesso && uri != null) {
            escopo.launch { Fotos.salvarDeUri(contexto, uri)?.let { foto = it } }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        CampoGrande(
            nome,
            { nome = Formatos.capitalizarPalavras(it) },
            "Nome comercial",
            obrigatorio = true,
            erro = erros["nome"],
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            CampoGrande(
                qtd,
                { novo -> qtd = novo.filter { it.isDigit() }.take(3) },
                "Quantidade de aplicações",
                Modifier.weight(1f),
                obrigatorio = true,
                erro = erros["qtd"],
                ajuda = "Quantas aplicações compõem o tratamento",
                tipoTeclado = KeyboardType.Number,
            )
            CampoGrande(
                laboratorio,
                { laboratorio = Formatos.capitalizarPalavras(it) },
                "Laboratório",
                Modifier.weight(1f),
                ajuda = "Opcional",
            )
        }
        Column {
            RotuloCampo("Composições e miligramagens (opcional)", false)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                compostos.forEachIndexed { indice, composto ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = composto.first,
                            onValueChange = {
                                compostos[indice] = composto.copy(first = Formatos.capitalizarPalavras(it))
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = {
                                Text(
                                    "Composição — ex.: Cianocobalamina",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(2f),
                        )
                        OutlinedTextField(
                            value = composto.second,
                            onValueChange = { compostos[indice] = composto.copy(second = it) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = {
                                Text(
                                    "Miligramagem",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                        )
                        if (compostos.size > 1) {
                            BotaoIcone(Icons.Filled.Close, "Remover composição", { compostos.removeAt(indice) })
                        }
                    }
                }
                BotaoGrandeContorno(
                    "Adicionar composição",
                    { compostos.add("" to "") },
                    icone = Icons.Filled.Add,
                )
            }
        }
        Column {
            RotuloCampo("Foto da caixa (opcional)", false)
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val fotoAtual = foto
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(150.dp)
                        .then(
                            if (fotoAtual != null) Modifier.clickable { visualizandoFoto = true }
                            else Modifier
                        ),
                ) {
                    if (fotoAtual != null) {
                        Box {
                            AsyncImage(
                                model = Fotos.arquivo(contexto, fotoAtual),
                                contentDescription = "Foto da caixa",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                            // Selo indicando que dá para tocar e ampliar.
                            Surface(
                                shape = RoundedCornerShape(topStart = 12.dp),
                                color = Color.Black.copy(alpha = 0.45f),
                                contentColor = Color.White,
                                modifier = Modifier.align(Alignment.BottomEnd),
                            ) {
                                Icon(
                                    Icons.Filled.ZoomIn,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(26.dp),
                                )
                            }
                        }
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp),
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BotaoGrandeContorno(
                        "Tirar foto",
                        {
                            try {
                                val uri = Fotos.uriParaCamera(contexto)
                                uriCamera = uri
                                abrirCamera.launch(uri)
                            } catch (e: Exception) {
                                Toast.makeText(contexto, "Não foi possível abrir a câmera.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        icone = Icons.Filled.PhotoCamera,
                    )
                    BotaoGrandeContorno(
                        "Escolher da galeria",
                        {
                            abrirGaleria.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        icone = Icons.Filled.Image,
                    )
                    if (foto != null) {
                        TextButton(onClick = { foto = null }) {
                            Text(
                                "Remover foto",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (aoCancelar != null) {
                BotaoGrandeContorno("Cancelar", aoCancelar, Modifier.weight(1f))
            }
            BotaoGrande(
                textoSalvar,
                {
                    val novosErros = buildMap {
                        if (nome.isBlank()) put("nome", "Informe o nome comercial")
                        if ((qtd.toIntOrNull() ?: 0) <= 0) put("qtd", "Informe a quantidade de aplicações")
                    }
                    erros = novosErros
                    if (novosErros.isEmpty()) {
                        aoSalvar(
                            Injecao(
                                id = original?.id ?: 0L,
                                nomeComercial = nome.trim(),
                                composicao = Formatos.serializarCompostos(compostos.toList()),
                                laboratorio = laboratorio.trim().ifBlank { null },
                                fotoArquivo = foto,
                                qtdAplicacoes = qtd.toInt(),
                            )
                        )
                    }
                },
                Modifier.weight(1f),
            )
        }
    }

    val fotoAberta = foto
    if (visualizandoFoto && fotoAberta != null) {
        VisualizadorImagem(
            arquivo = Fotos.arquivo(contexto, fotoAberta),
            aoFechar = { visualizandoFoto = false },
        )
    }
}
