# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Sobre

App Android nativo (Kotlin + Jetpack Compose) da **Farmácia Vigor** para gerenciar injeções aplicadas em clientes durante tratamentos. Roda num tablet compartilhado (Galaxy Tab S6 Lite / SM-P615, Android 13) usado por vários atendentes. Todos os dados ficam locais (Room); não há backend.

**Convenção importante:** todo o código é escrito em **português** — nomes de pacote, classes, funções, variáveis e strings de UI (`Navegador`, `Tratamento`, `registrarAplicacao`, etc.). Mantenha esse padrão ao adicionar código.

## Comandos

`gradle` e `adb` **não estão no PATH**. Use o wrapper e o caminho completo do adb:

```bash
# Compilar o APK debug
./gradlew :app:assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# Instalar / rodar no tablet (conectado por adb sem fio)
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
~/Library/Android/sdk/platform-tools/adb shell am start -n com.farmaciavigor.app/.MainActivity

# Ver o motivo de um crash
~/Library/Android/sdk/platform-tools/adb logcat -d -b crash | grep -A30 farmaciavigor

# Screenshot para verificação visual
~/Library/Android/sdk/platform-tools/adb exec-out screencap -p > /tmp/tela.png
```

Não há testes automatizados no projeto — a verificação é feita instalando no tablet e inspecionando por screenshot/logcat.

## Toolchain (bleeding-edge — gotchas)

- AGP **9.2.1**, Kotlin **2.2.10**, Gradle **9.4.1**, `compileSdk` 36.1 (usa a DSL nova `compileSdk { version = release(36) { minorApiLevel = 1 } }`), `minSdk` 33.
- Room usa **KSP**. Com o Kotlin embutido do AGP 9 é obrigatório `android.disallowKotlinSourceSets=false` em `gradle.properties` — sem isso o build falha com "Using kotlin.sourceSets DSL to add Kotlin sources is not allowed".
- Versões e plugins centralizados em `gradle/libs.versions.toml` (version catalog).

## Arquitetura

Activity única (`MainActivity`) com Compose. Sem Navigation-Compose e sem ViewModels — o estado vive em objetos e classes simples.

### Navegação (`Nucleo.kt`)
- `Tela` é uma sealed interface; cada tela é um `data object`/`data class`. `Navegador` mantém uma pilha (`mutableStateListOf`), com `ir()`, `voltar()`, `definir()`. `MainActivity.AppRaiz` renderiza a pilha via `AnimatedContent`, usando `nav.avancando` para escolher a direção do slide.
- `nav.abaHome` (índice da aba da tela principal) mora no `Navegador`, **fora** da `TelaPrincipal`, de propósito: assim abrir um formulário e voltar preserva a aba (ex.: editar injeção volta para "Injeções", não "Tratamentos").
- `Sessao` (objeto global) guarda o `Funcionario` escolhido no "login". É **zerado a cada criação da Activity** (`remember { Sessao.funcionario = null; Navegador() }`) para que reabrir o app sempre comece limpo na tela de funcionários.

### Dados (`dados/`)
- `Entidades.kt`: `Funcionario`, `Cliente`, `Injecao`, `Tratamento`, `Aplicacao`. `TratamentoResumo` e `AplicacaoHistorico` são projeções `@Embedded` de queries agregadas (não são tabelas).
- DAOs (`Daos.kt`) retornam `Flow` (a UI coleta com `collectAsState`). Somas/contagens ficam em subqueries dentro do próprio SQL.
- `Repositorio.kt` concentra a regra de negócio transacional. **`registrarAplicacao`** é o coração do app: um `Tratamento` é criado *implicitamente* na primeira aplicação de um par cliente+injeção e se **encerra sozinho** (`encerrado = true`) quando o nº de aplicações atinge `totalDoses`. `encerrado = true` cobre tanto concluídos quanto encerrados manualmente — a lista de "em tratamento" filtra por `encerrado = 0`.
- **`abrirTratamento`** cria o tratamento **sem aplicação nenhuma** (botão "Só abrir o tratamento" na `TelaNovaAplicacao`), para deixar salvo o que o cliente comprou e aplicar depois. Um tratamento com `aplicadas == 0` é válido em todo o app (`TratamentoResumo.aguardandoPrimeira`): aparece na lista "em tratamento" com a etiqueta "Aguardando 1ª aplicação", e a primeira aplicação entra nele naturalmente porque `registrarAplicacao` consulta `ativoDoPar` antes de criar.
- Pagamento é `antecipado` (tudo pago na abertura → toda aplicação já entra paga) **ou** por aplicação (cada `Aplicacao` tem seu `pago`, marcável depois).

### Backup (`util/Backup.kt` + cartão em `TelaPrincipal.SecaoBackup`)
- **Backup manual** em `.zip` salvo pelo usuário via seletor do sistema (Google Drive, etc.): `exportar` gera o banco com `VACUUM INTO` (arquivo único e consistente, sem depender de WAL/SHM) mais a pasta `fotos/`; `importar` **valida o zip antes de apagar qualquer coisa**, substitui banco + fotos e chama `reiniciarApp` — o reinício do processo é obrigatório porque `FarmaciaApp.banco` é `by lazy` (não reabre no mesmo processo).
- **Auto Backup do Android** ligado (`allowBackup=true`), mas `res/xml/data_extraction_rules.xml` (e `backup_rules.xml` p/ API <31) **exclui `fotos/`** por causa do limite de 25 MB da nuvem; `device-transfer` leva tudo. Guarda banco + prefs na conta Google do tablet e restaura sozinho ao reinstalar.
- Sync automático via Drive API foi considerado e **adiado** (exige projeto no Google Cloud + OAuth + tela de "app não verificado").

### UI (`ui/`)
- `ui/tema/Tema.kt`: paleta off-white + vermelho e tipografia **ampliada** (público variado, inclusive idosos). Cores nomeadas exportadas (`VermelhoVigor`, `VerdeConfirmacao`, `AmbarAviso`) para uso direto.
- `ui/comum/Componentes.kt`: biblioteca de componentes de toque grande reutilizados em todas as telas (`BotaoGrande`, `CampoGrande`, `SeletorOpcoes`, `DialogoGrande`, `DialogoSelecao`, `VisualizadorImagem` com pinça-zoom, `TransformacaoMascara`). Prefira reusar daqui a criar widgets novos.
- **Largura apertada quebra texto no meio da palavra** ("Registra r"): num `Row`, os filhos sem peso são medidos primeiro e sobram poucos dp para o `weight(1f)`. Não coloque botão de texto ao lado de título/outro botão à mão — use **`ParDeBotoes`** (lado a lado no tablet, empilhado no celular) e **`LinhaEtiquetas`** (`FlowRow`, a etiqueta inteira desce em vez de espremer). O breakpoint mora em **`ehTablet()`**, não replique o `>= 600`.
- **Layout responsivo (celular × tablet)**: `TelaPrincipal` decide o layout por `LocalConfiguration.smallestScreenWidthDp >= 600` (tablet ≈ 800, celular ≈ 548). **Cuidado: não use breakpoint em `maxWidth`/dp da composição — o zoom sobrescreve `LocalDensity` e encolheria os dp, fazendo o tablet cair no layout de celular no zoom alto.** No tablet, a `BarraLateral` fica fixa num `Row` (o layout original, dispositivo principal). No celular ela vira um `ModalNavigationDrawer` aberto por um botão de menu passado via `aoAbrirMenu` ao `Cabecalho`; quando `aoAbrirMenu != null` (= celular) os botões de ação do cabeçalho ficam compactos (só o ícone). As telas de formulário/detalhe já rolam e não precisam de tratamento por tamanho.
- **Zoom da tela**: configurável pelo usuário; implementado em `MainActivity` sobrescrevendo `LocalDensity` (escala tudo). Persistido em `Ajustes` (SharedPreferences).
- **Tela cheia**: modo imersivo (barras de sistema escondidas) ligado em `MainActivity.esconderBarrasDoSistema()` e reforçado em `onWindowFocusChanged`.
- **Máscaras (CPF/telefone)**: os campos guardam **apenas dígitos**; a pontuação é desenhada por `VisualTransformation` (`TransformacaoMascara`), o que mantém o cursor na posição certa. Helpers em `util/Formatos.kt` (`mascaraCpf`, `cpfValido`, `capitalizarPalavras`, serialização de composições).
- **Fotos** (`util/Fotos.kt`): copiadas para `filesDir/fotos`; captura via câmera usa `FileProvider` (autoridade `${applicationId}.fileprovider`, ver `res/xml/file_paths.xml`).
