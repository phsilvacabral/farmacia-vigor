package com.farmaciavigor.app.ui.comum

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.farmaciavigor.app.Ajustes
import com.farmaciavigor.app.ui.tema.AmbarAviso
import com.farmaciavigor.app.util.Formatos
import java.io.File
import kotlin.math.roundToInt

/**
 * Baseado na config real do aparelho (não no zoom, que altera a densidade e faria o
 * tablet cair no layout de celular no zoom alto): tablet ≈ 800dp, celular ≈ 548dp.
 */
@Composable
fun ehTablet(): Boolean = LocalConfiguration.current.smallestScreenWidthDp >= 600

@Composable
fun BotaoGrande(
    texto: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    icone: ImageVector? = null,
) {
    Button(
        onClick = aoClicar,
        enabled = habilitado,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 26.dp, vertical = 16.dp),
        modifier = modifier.heightIn(min = 62.dp),
    ) {
        if (icone != null) {
            Icon(icone, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun BotaoGrandeContorno(
    texto: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    icone: ImageVector? = null,
) {
    OutlinedButton(
        onClick = aoClicar,
        enabled = habilitado,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = 26.dp, vertical = 16.dp),
        modifier = modifier.heightIn(min = 62.dp),
    ) {
        if (icone != null) {
            Icon(icone, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

/** Botão circular de ícone com área de toque generosa. */
@Composable
fun BotaoIcone(
    icone: ImageVector,
    descricao: String?,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    cor: Color = MaterialTheme.colorScheme.surfaceVariant,
    corConteudo: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        onClick = aoClicar,
        shape = CircleShape,
        color = cor,
        contentColor = corConteudo,
        modifier = modifier.size(56.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icone, contentDescription = descricao, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
fun Cabecalho(
    titulo: String,
    modifier: Modifier = Modifier,
    aoVoltar: (() -> Unit)? = null,
    aoAbrirMenu: (() -> Unit)? = null,
    acoes: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
    ) {
        // Voltar (telas empilhadas) tem prioridade; senão, o menu (só no celular).
        if (aoVoltar != null) {
            BotaoIcone(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", aoVoltar)
            Spacer(Modifier.width(18.dp))
        } else if (aoAbrirMenu != null) {
            BotaoIcone(Icons.Filled.Menu, "Abrir menu", aoAbrirMenu)
            Spacer(Modifier.width(18.dp))
        }
        // Num Row os filhos sem peso são medidos primeiro: uma ação larga come toda a
        // largura e o título sobra com poucos dp, quebrando letra a letra. O limite de
        // linhas segura o estrago caso o título seja longo demais para o espaço restante.
        Text(
            titulo,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        acoes()
    }
}

@Composable
fun RotuloCampo(texto: String, obrigatorio: Boolean, modifier: Modifier = Modifier) {
    Row(modifier.padding(bottom = 8.dp)) {
        Text(texto, style = MaterialTheme.typography.titleMedium)
        if (obrigatorio) {
            Text(
                " *",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun MensagemErro(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(top = 6.dp, start = 4.dp),
    )
}

@Composable
fun CampoGrande(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    obrigatorio: Boolean = false,
    erro: String? = null,
    aviso: String? = null,
    ajuda: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    linhas: Int = 1,
    transformacao: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier) {
        RotuloCampo(rotulo, obrigatorio)
        OutlinedTextField(
            value = valor,
            onValueChange = aoMudar,
            isError = erro != null,
            singleLine = linhas == 1,
            minLines = linhas,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
            shape = RoundedCornerShape(14.dp),
            visualTransformation = transformacao,
            modifier = Modifier.fillMaxWidth(),
        )
        when {
            erro != null -> MensagemErro(erro)
            aviso != null -> Text(
                aviso,
                style = MaterialTheme.typography.bodyMedium,
                color = AmbarAviso,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp),
            )
            ajuda != null -> Text(
                ajuda,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp),
            )
        }
    }
}

@Composable
fun CampoBusca(
    valor: String,
    aoMudar: (String) -> Unit,
    modifier: Modifier = Modifier,
    dica: String = "Buscar pelo nome…",
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = {
            Text(dica, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        leadingIcon = {
            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(28.dp))
        },
        shape = RoundedCornerShape(50),
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Data digitada no teclado numérico, sem calendário: o campo guarda **apenas os dígitos**
 * (ddMMyyyy) e as barras são desenhadas pela máscara visual, como no CPF e no telefone.
 * A validade é conferida por `Formatos.erroData` / `Formatos.dataParaMillis`.
 */
@Composable
fun CampoData(
    digitos: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    obrigatorio: Boolean = false,
    erro: String? = null,
    ajuda: String? = "dd/mm/aaaa",
) {
    val mascara = remember { TransformacaoMascara(Formatos::mascaraData) }
    CampoGrande(
        digitos,
        { novo -> aoMudar(novo.filter { it.isDigit() }.take(8)) },
        rotulo,
        modifier,
        obrigatorio = obrigatorio,
        erro = erro,
        ajuda = ajuda,
        tipoTeclado = KeyboardType.Number,
        transformacao = mascara,
    )
}

/** Opções lado a lado em botões grandes (sexo, forma de pagamento…). */
@Composable
fun SeletorOpcoes(
    rotulo: String,
    opcoes: List<String>,
    selecionada: String?,
    aoSelecionar: (String) -> Unit,
    modifier: Modifier = Modifier,
    obrigatorio: Boolean = false,
    erro: String? = null,
) {
    Column(modifier) {
        RotuloCampo(rotulo, obrigatorio)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            opcoes.forEach { opcao ->
                val ativa = opcao == selecionada
                Surface(
                    onClick = { aoSelecionar(opcao) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (ativa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (ativa) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = if (ativa) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 62.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(opcao, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        if (erro != null) MensagemErro(erro)
    }
}

/** Linha com interruptor grande, clicável na área toda. */
@Composable
fun LinhaInterruptor(
    texto: String,
    valor: Boolean,
    aoMudar: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = { aoMudar(!valor) },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Text(texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = valor, onCheckedChange = aoMudar)
        }
    }
}

@Composable
fun CartaoBase(
    modifier: Modifier = Modifier,
    aoClicar: (() -> Unit)? = null,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    val forma = RoundedCornerShape(20.dp)
    val borda = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    val cor = MaterialTheme.colorScheme.surface
    if (aoClicar != null) {
        Surface(onClick = aoClicar, shape = forma, color = cor, border = borda, modifier = modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), content = conteudo)
        }
    } else {
        Surface(shape = forma, color = cor, border = borda, modifier = modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), content = conteudo)
        }
    }
}

/** Selo colorido pequeno ("3 não pagas", "Concluído"…). */
@Composable
fun Etiqueta(texto: String, cor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50),
        color = cor.copy(alpha = 0.14f),
        contentColor = cor,
        modifier = modifier,
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelMedium,
            // Etiqueta é uma pílula: nunca quebra por dentro. Se não couber na linha,
            // quem cede é a LinhaEtiquetas, passando a pílula inteira para baixo.
            softWrap = false,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

/**
 * Botão principal com um secundário opcional ao lado. No tablet ficam lado a lado;
 * no celular meia largura quebraria rótulos no meio da palavra ("Registra r"), então
 * empilha em largura total — o que ainda dá um alvo de toque maior.
 */
@Composable
fun ParDeBotoes(
    primario: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    secundario: (@Composable (Modifier) -> Unit)? = null,
) {
    when {
        secundario == null -> primario(modifier.fillMaxWidth())
        ehTablet() -> Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier) {
            secundario(Modifier.weight(1f))
            primario(Modifier.weight(1f))
        }
        else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
            primario(Modifier.fillMaxWidth())
            secundario(Modifier.fillMaxWidth())
        }
    }
}

/** Linha de etiquetas que passa para a linha de baixo quando a largura acaba. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinhaEtiquetas(
    modifier: Modifier = Modifier,
    conteudo: @Composable FlowRowScope.() -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
        content = conteudo,
    )
}

@Composable
fun MensagemVazia(icone: ImageVector, titulo: String, texto: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 48.dp),
    ) {
        Icon(
            icone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(18.dp))
        Text(titulo, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Círculo com a inicial do nome, usado como avatar. */
@Composable
fun Avatar(nome: String, modifier: Modifier = Modifier, tamanho: Int = 72) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(tamanho.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
    ) {
        Text(
            nome.trim().take(1).uppercase().ifBlank { "?" },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
fun LogotipoVigor(modifier: Modifier = Modifier, tamanho: Int = 88) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(tamanho.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
    ) {
        Icon(
            Icons.Filled.LocalPharmacy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size((tamanho * 0.55f).dp),
        )
    }
}

/** Controle de zoom (A- / A+) disponível na tela inicial e nos ajustes. */
@Composable
fun ControleZoom(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        BotaoIcone(Icons.Filled.ZoomOut, "Diminuir zoom", { Ajustes.definirZoom(Ajustes.zoom - 0.1f) })
        Text(
            "${(Ajustes.zoom * 100).roundToInt()}%",
            style = MaterialTheme.typography.titleMedium,
        )
        BotaoIcone(Icons.Filled.ZoomIn, "Aumentar zoom", { Ajustes.definirZoom(Ajustes.zoom + 0.1f) })
    }
}

/** Caixa de diálogo larga com título e botão de fechar, com rolagem própria do conteúdo. */
@Composable
fun DialogoGrande(
    titulo: String,
    aoFechar: () -> Unit,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = aoFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 720.dp)
                .fillMaxWidth(),
        ) {
            Column(Modifier.padding(28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    BotaoIcone(Icons.Filled.Close, "Fechar", aoFechar)
                }
                Spacer(Modifier.height(22.dp))
                conteudo()
            }
        }
    }
}

/** Lista de escolha com busca e atalho para cadastrar um item novo. */
@Composable
fun <T> DialogoSelecao(
    titulo: String,
    itens: List<T>,
    textoPrincipal: (T) -> String,
    aoEscolher: (T) -> Unit,
    aoCriarNovo: () -> Unit,
    textoCriarNovo: String,
    aoFechar: () -> Unit,
    textoSecundario: (T) -> String? = { null },
) {
    var busca by remember { mutableStateOf("") }
    val filtrados = if (busca.isBlank()) itens
    else itens.filter { Formatos.paraBusca(textoPrincipal(it)).contains(Formatos.paraBusca(busca)) }

    Dialog(
        onDismissRequest = aoFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
        ) {
            Column(Modifier.padding(28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    BotaoIcone(Icons.Filled.Close, "Fechar", aoFechar)
                }
                Spacer(Modifier.height(18.dp))
                CampoBusca(busca, { busca = it })
                Spacer(Modifier.height(16.dp))
                if (filtrados.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            if (itens.isEmpty()) "Nenhum cadastro ainda." else "Nada encontrado para \"$busca\".",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        items(filtrados) { item ->
                            Surface(
                                onClick = { aoEscolher(item) },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                                    Text(textoPrincipal(item), style = MaterialTheme.typography.titleMedium)
                                    textoSecundario(item)?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                BotaoGrande(textoCriarNovo, aoCriarNovo, Modifier.fillMaxWidth(), icone = Icons.Filled.Add)
            }
        }
    }
}

/**
 * Exibe uma máscara (CPF, telefone…) sobre um campo que guarda apenas dígitos,
 * mapeando o cursor corretamente através da pontuação inserida.
 */
class TransformacaoMascara(private val mascara: (String) -> String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatado = mascara(text.text)
        return TransformedText(
            AnnotatedString(formatado),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 0) return 0
                    var digitos = 0
                    formatado.forEachIndexed { indice, caractere ->
                        if (caractere.isDigit()) {
                            digitos++
                            if (digitos == offset) return indice + 1
                        }
                    }
                    return formatado.length
                }

                override fun transformedToOriginal(offset: Int): Int =
                    formatado.take(offset.coerceIn(0, formatado.length)).count { it.isDigit() }
            },
        )
    }
}

/**
 * Abre uma imagem em tela cheia com pinça para zoom e arraste para mover.
 * Toque duplo alterna entre ampliado e tamanho normal.
 */
@Composable
fun VisualizadorImagem(arquivo: File, aoFechar: () -> Unit) {
    Dialog(onDismissRequest = aoFechar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var escala by remember { mutableStateOf(1f) }
        var deslocX by remember { mutableStateOf(0f) }
        var deslocY by remember { mutableStateOf(0f) }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { aoFechar() },
                        onDoubleTap = {
                            if (escala > 1f) {
                                escala = 1f; deslocX = 0f; deslocY = 0f
                            } else {
                                escala = 2.5f
                            }
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        escala = (escala * zoom).coerceIn(1f, 6f)
                        if (escala > 1f) {
                            deslocX += pan.x
                            deslocY += pan.y
                        } else {
                            deslocX = 0f; deslocY = 0f
                        }
                    }
                },
        ) {
            AsyncImage(
                model = arquivo,
                contentDescription = "Foto da caixa ampliada",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = escala,
                        scaleY = escala,
                        translationX = deslocX,
                        translationY = deslocY,
                    ),
            )
            BotaoIcone(
                Icons.Filled.Close,
                "Fechar",
                aoFechar,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp),
                cor = Color.White.copy(alpha = 0.16f),
                corConteudo = Color.White,
            )
        }
    }
}
