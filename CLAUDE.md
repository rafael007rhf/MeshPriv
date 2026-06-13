# CLAUDE.md — MeshPriv

> Leia este arquivo inteiro antes de escrever qualquer linha de código.
> Ele define o projeto, a arquitetura, as convenções e as restrições que não podem ser violadas.

---

## 1. O que é o MeshPriv

MeshPriv é um aplicativo Android de comunicação P2P descentralizada.
Dispositivos Android criam uma rede mesh local entre si sem internet, sem servidores e sem operadoras.
Cada celular funciona simultaneamente como cliente, servidor e roteador da rede.

**Contexto acadêmico:** o projeto gera um artigo científico para o Latin.Science 2026
(trilha acadêmica do Latinoware, Foz do Iguaçu, 14–16 outubro de 2026).
O prazo de submissão do artigo é **20 de julho de 2026**.
O código precisa estar testável em dispositivos reais antes disso.

**Linguagem do projeto:** português brasileiro (variáveis, comentários, commits, logs de UI).
Exceção: nomes de classes e funções seguem convenção Kotlin em inglês.

---

## 2. Escopo do MVP — o que entra e o que não entra

### ✅ Entra no MVP (obrigatório)

| Funcionalidade | Justificativa |
|---|---|
| Descoberta de dispositivos via Nearby Connections API | Core da rede mesh |
| Geração de identidade local (chave pública + ID + apelido) | Necessário para criptografia e roteamento |
| Criptografia E2E (ECDH Curve25519 + AES-GCM) | Prova o conceito sem servidor |
| Roteamento multi-hop via flooding com TTL e deduplicação por Message ID | Gera os dados de métricas do artigo |
| Mensagens texto 1-to-1 (diretas e roteadas) | Caso de uso principal |
| Coleta de métricas: latência, hop count, taxa de entrega, consumo de bateria | **Esses dados são o experimento do artigo** |
| UI mínima funcional em Jetpack Compose | Necessária para testes reais |
| Persistência local com Room (mensagens, peers, métricas) | Permite análise pós-teste |
| Exportação de métricas em CSV | Para análise offline e inclusão no artigo |

### ❌ Fora do MVP (não implementar agora)

| Funcionalidade | Motivo da exclusão |
|---|---|
| Wi-Fi Aware | Suporte fragmentado nos fabricantes — causa falha silenciosa |
| X3DH + Double Ratchet | Alta complexidade; ECDH + AES-GCM já prova criptografia E2E |
| Store-and-Forward (relay offline) | Aumenta escopo sem gerar nova métrica para o short paper |
| Canais comunitários / grupos | UI e lógica extras sem impacto nas métricas core |
| Modo SOS / emergência | Pode ser simulado manualmente nos experimentos |
| Visualização gráfica da topologia da rede | Relevante para V2, não para o MVP |
| Notificações push | Fora do escopo técnico atual |
| Multiplataforma (iOS / Linux) | Fora do escopo completamente |
| Autenticação por conta / e-mail | Contradiz o modelo de identidade local |
| Qualquer chamada a servidor externo | Contradiz o propósito do projeto |

**Regra geral:** se uma feature não gera dado mensurável para o artigo, ela fica de fora.

---

## 3. Stack tecnológica

### Linguagem e plataforma
- **Kotlin** 2.x (sem Java)
- **Android SDK:** minSdk 26 (Android 8.0), targetSdk 35
- **Gradle:** Kotlin DSL (`build.gradle.kts`)
- **Version Catalog:** `libs.versions.toml`

### UI
- **Jetpack Compose** (Material Design 3)
- **Navigation Compose** com rotas tipadas
- Sem XML layouts — tudo em Compose

### Arquitetura
- **Clean Architecture** com três camadas: `ui`, `domain`, `data`
- **MVVM** — ViewModels expõem StateFlow, UI observa com `collectAsStateWithLifecycle`
- **Unidirectional Data Flow (UDF):** eventos sobem, estado desce

### Injeção de dependência
- **Hilt** — todos os módulos em `di/`

### Assincronicidade
- **Kotlin Coroutines** + **Flow**
- Dispatchers explícitos (`Dispatchers.IO` para I/O, `Dispatchers.Default` para CPU)
- Nunca usar `runBlocking` fora de testes

### Persistência
- **Room** com entidades separadas por domínio
- Migrations versionadas (nunca `fallbackToDestructiveMigration` em produção)

### Criptografia
- **Tink** (Google) para operações criptográficas de alto nível
- ECDH com Curve25519 para troca de chave
- AES-GCM 256 para cifrar o payload das mensagens
- Chaves persistidas com **EncryptedSharedPreferences**

### Rede mesh
- **Nearby Connections API** (`com.google.android.gms:play-services-nearby`)
- Estratégia: `Strategy.P2P_CLUSTER`
- Sem Wi-Fi Aware, sem Bluetooth clássico direto

### Métricas
- `BatteryManager` API para monitorar consumo durante testes
- Timestamps com `System.currentTimeMillis()` — precisão suficiente para ms
- Exportação via `FileProvider` + CSV

### Testes
- **JUnit 5** para testes unitários
- **MockK** para mocks em Kotlin
- **Turbine** para testar Flows
- **Robolectric** para testes de componentes Android sem emulador

---

## 4. Estrutura de pastas

```
meshpriv/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/br/dev/meshpriv/
│   │   │   │   ├── MeshPrivApplication.kt       ← Application class com @HiltAndroidApp
│   │   │   │   ├── MainActivity.kt              ← Single Activity com NavHost
│   │   │   │   │
│   │   │   │   ├── ui/                          ← CAMADA DE UI
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── AppNavGraph.kt       ← rotas e NavHost
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   ├── chat/
│   │   │   │   │   │   ├── ChatScreen.kt
│   │   │   │   │   │   └── ChatViewModel.kt
│   │   │   │   │   ├── peers/
│   │   │   │   │   │   ├── PeersScreen.kt
│   │   │   │   │   │   └── PeersViewModel.kt
│   │   │   │   │   ├── metrics/
│   │   │   │   │   │   ├── MetricsScreen.kt
│   │   │   │   │   │   └── MetricsViewModel.kt
│   │   │   │   │   └── components/              ← composables reutilizáveis
│   │   │   │   │       ├── MessageBubble.kt
│   │   │   │   │       ├── PeerCard.kt
│   │   │   │   │       └── MetricRow.kt
│   │   │   │   │
│   │   │   │   ├── domain/                      ← CAMADA DE DOMÍNIO
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Peer.kt              ← modelo de peer na rede
│   │   │   │   │   │   ├── Message.kt           ← modelo de mensagem
│   │   │   │   │   │   ├── MeshPacket.kt        ← pacote de roteamento
│   │   │   │   │   │   ├── LocalIdentity.kt     ← identidade local do dispositivo
│   │   │   │   │   │   └── DeliveryMetric.kt    ← métrica de entrega
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── MessageRepository.kt ← interface
│   │   │   │   │   │   ├── PeerRepository.kt    ← interface
│   │   │   │   │   │   └── MetricsRepository.kt ← interface
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── SendMessageUseCase.kt
│   │   │   │   │       ├── ObservePeersUseCase.kt
│   │   │   │   │       ├── ObserveMessagesUseCase.kt
│   │   │   │   │       └── ExportMetricsCsvUseCase.kt
│   │   │   │   │
│   │   │   │   ├── data/                        ← CAMADA DE DADOS
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── MeshPrivDatabase.kt  ← Room Database
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── MessageDao.kt
│   │   │   │   │   │   │   ├── PeerDao.kt
│   │   │   │   │   │   │   └── MetricDao.kt
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       ├── MessageEntity.kt
│   │   │   │   │   │       ├── PeerEntity.kt
│   │   │   │   │   │       └── MetricEntity.kt
│   │   │   │   │   ├── mesh/
│   │   │   │   │   │   ├── NearbyConnectionsManager.kt  ← discovery + connections
│   │   │   │   │   │   ├── MessageRouter.kt             ← flooding + TTL
│   │   │   │   │   │   └── SeenMessageCache.kt          ← deduplicação
│   │   │   │   │   ├── crypto/
│   │   │   │   │   │   ├── CryptoManager.kt             ← ECDH + AES-GCM
│   │   │   │   │   │   └── IdentityManager.kt           ← geração e persistência de chaves
│   │   │   │   │   ├── metrics/
│   │   │   │   │   │   ├── MetricsCollector.kt          ← coleta e registro
│   │   │   │   │   │   └── BatteryMonitor.kt            ← BatteryManager wrapper
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── MessageRepositoryImpl.kt
│   │   │   │   │       ├── PeerRepositoryImpl.kt
│   │   │   │   │       └── MetricsRepositoryImpl.kt
│   │   │   │   │
│   │   │   │   └── di/                          ← MÓDULOS HILT
│   │   │   │       ├── DatabaseModule.kt
│   │   │   │       ├── MeshModule.kt
│   │   │   │       ├── CryptoModule.kt
│   │   │   │       └── RepositoryModule.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   └── xml/
│   │   │   │       └── file_paths.xml           ← para FileProvider (exportação CSV)
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── test/                                ← testes unitários (JVM)
│   │   └── androidTest/                         ← testes instrumentados
│   │
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                       ← version catalog
├── build.gradle.kts
├── settings.gradle.kts
└── CLAUDE.md                                    ← este arquivo
```

---

## 5. Modelos de domínio — campos obrigatórios

Esses modelos são o contrato entre camadas. Não altere campos sem atualizar todas as camadas.

### `LocalIdentity`
```kotlin
data class LocalIdentity(
    val nodeId: String,          // hash SHA-256 da chave pública, 8 chars hex maiúsculo
    val nickname: String,        // apelido definido pelo usuário
    val publicKey: ByteArray,    // chave pública ECDH Curve25519
    val privateKey: ByteArray    // chave privada — NUNCA logar, NUNCA serializar para rede
)
```

### `Peer`
```kotlin
data class Peer(
    val nodeId: String,          // identificador único do peer
    val nickname: String,
    val publicKey: ByteArray,
    val endpointId: String,      // ID do Nearby Connections (volátil, muda por sessão)
    val signalStrength: Int,     // 0–100, estimado pelo Nearby Connections
    val lastSeenAt: Long,        // timestamp Unix ms
    val isConnected: Boolean
)
```

### `Message`
```kotlin
data class Message(
    val messageId: String,       // UUID v4
    val senderId: String,        // nodeId do remetente
    val recipientId: String,     // nodeId do destinatário
    val content: String,         // texto descriptografado (só disponível no destinatário)
    val sentAt: Long,            // timestamp de criação (remetente)
    val receivedAt: Long?,       // timestamp de recebimento (null se ainda não entregue)
    val hopCount: Int,           // quantos saltos a mensagem deu até chegar
    val status: MessageStatus    // SENDING, DELIVERED, FAILED
)

// Lado do remetente: SENDING ao despachar, DELIVERED quando o ACK do destinatário retorna.
// Sem timeout no MVP: se o ACK nunca chegar, o status permanece SENDING.
enum class MessageStatus { SENDING, DELIVERED, FAILED }
```

### `MeshPacket`
```kotlin
// MESSAGE transporta conteúdo cifrado; ACK confirma a entrega ao remetente original
@Serializable
enum class PacketType { MESSAGE, ACK }

@Serializable
data class MeshPacket(
    val packetId: String,        // UUID v4 — usado para deduplicação
    val sourceId: String,        // nodeId do remetente original
    val destinationId: String,   // nodeId do destinatário final
    val type: PacketType = PacketType.MESSAGE, // default MESSAGE: compatibilidade com pacotes antigos
    val encryptedPayload: ByteArray, // MESSAGE: cifrado com AES-GCM | ACK: messageId original em claro
    val senderPublicKey: ByteArray,  // chave pública do remetente (para decriptografia)
    val ttl: Int,                // Time To Live — começa em 7, decrementado a cada hop
    val hopCount: Int,           // incrementado a cada hop
    val createdAt: Long          // timestamp de criação no remetente
)
```

**Semântica do ACK:** quando uma MESSAGE chega ao destinatário final, ele gera um pacote
ACK de volta para o remetente original: `sourceId` = quem recebeu, `destinationId` = quem
enviou, payload = o `messageId` confirmado. O payload do ACK **não é cifrado** no MVP — ele
só referencia um ID que já trafega em claro no `packetId` da MESSAGE, sem conteúdo
confidencial.

### `DeliveryMetric`
```kotlin
data class DeliveryMetric(
    val metricId: String,        // UUID v4
    val messageId: String,       // referência à mensagem
    val sourceId: String,
    val destinationId: String,
    val latencyMs: Long,         // receivedAt - sentAt
    val hopCount: Int,
    val delivered: Boolean,
    val batteryLevelStart: Int,  // 0–100
    val batteryLevelEnd: Int,    // 0–100
    val networkSize: Int,        // número de peers conectados no momento do envio
    val recordedAt: Long
)
```

---

## 6. Camada de mesh — comportamento esperado

### `NearbyConnectionsManager`

Responsabilidade: gerenciar todo o ciclo de vida da Nearby Connections API.

**Comportamento:**
- Ao iniciar, chamar `startAdvertising` e `startDiscovery` simultaneamente com `Strategy.P2P_CLUSTER`
- Service ID fixo: `"br.dev.meshpriv.mesh"`
- Ao descobrir um endpoint, solicitar conexão automaticamente (`requestConnection`)
- Ao receber solicitação de conexão, aceitar automaticamente (`acceptConnection`)
- Ao conectar um peer, atualizar o `PeerRepository` com `isConnected = true`
- Ao desconectar, atualizar `isConnected = false` mas manter o peer no banco (histórico)
- Expor `connectedPeers: StateFlow<List<Peer>>`
- Expor `incomingPackets: SharedFlow<Pair<String, ByteArray>>` (endpointId, rawBytes)
- Serializar e deserializar `MeshPacket` com `kotlinx.serialization` (JSON → ByteArray)

**Restrições:**
- Nunca usar callbacks Nearby fora desta classe
- Erros da API Nearby devem ser capturados e emitidos como eventos (não lançar exceção)
- Reconectar automaticamente em caso de falha de conexão com um peer conhecido

### `MessageRouter`

Responsabilidade: decidir o que fazer com cada `MeshPacket` recebido ou a enviar.

**Algoritmo de recebimento:**
```
1. Verificar se packetId já está no SeenMessageCache
   → Se sim: DESCARTAR (deduplicação — vale para MESSAGE e ACK)
   → Se não: REGISTRAR no cache

2. Verificar se destinationId == localNodeId
   → Se sim e type == MESSAGE:
       ENTREGAR à camada de domínio (descriptografar + salvar + notificar)
       e ENVIAR um ACK de volta ao remetente original (ver regras abaixo)
   → Se sim e type == ACK:
       marcar a mensagem original como DELIVERED, registrar receivedAt
       e emitir evento para as métricas — NUNCA gerar outro ACK (guarda contra loop)
   → Se não: continuar

3. Verificar se ttl <= 0
   → Se sim: DESCARTAR e registrar falha de entrega nas métricas
   → Se não: continuar

4. RETRANSMITIR para todos os peers conectados exceto o peer de origem
   com ttl decrementado e hopCount incrementado
   (um ACK em trânsito num nó relay é só encaminhado, nunca tratado como entregue)
```

**Regras do ACK de entrega:**
- Gerado apenas quando uma MESSAGE é entregue com sucesso no destinatário final
  (falha de decriptografia não gera ACK)
- packetId NOVO (UUID), `type=ACK`, `ttl=7`, `hopCount=0`,
  `sourceId` = destinatário original, `destinationId` = remetente original,
  payload = messageId confirmado em claro
- Roteado de volta pelo mesmo flooding (dedup + TTL + retransmissão idênticos à MESSAGE)
- Registrar o packetId do ACK no SeenMessageCache antes de enviar (não reprocessar o eco)
- Um ACK NUNCA gera outro ACK — sem isso o protocolo entraria em loop infinito

**Algoritmo de envio:**
```
1. Receber Message do domínio
2. Buscar chave pública do destinatário no PeerRepository
3. Cifrar payload com CryptoManager
4. Criar MeshPacket com type=MESSAGE, ttl=7, hopCount=0, packetId=UUID
5. Registrar packetId no SeenMessageCache (para não reprocessar eco)
6. Enviar para todos os peers conectados via NearbyConnectionsManager
7. Registrar timestamp de envio nas métricas
8. Status fica SENDING até o ACK retornar (sem timeout no MVP:
   sem ACK, permanece SENDING)
```

### `SeenMessageCache`

Responsabilidade: evitar reprocessamento de pacotes já vistos (loops).

- Estrutura interna: `LinkedHashMap<String, Long>` (packetId → timestamp)
- Tamanho máximo: 500 entradas (remover a mais antiga quando exceder)
- TTL do cache: 10 minutos (entradas mais antigas que isso são removidas periodicamente)
- Thread-safe via `@Synchronized` ou `Mutex`

---

## 7. Camada de criptografia — comportamento esperado

### `IdentityManager`

Responsabilidade: criar e persistir a identidade local do dispositivo.

**Ao iniciar o app pela primeira vez:**
1. Gerar par de chaves ECDH Curve25519 via Tink (`KeysetHandle` com `EciesAeadHkdfPrivateKey`)
2. Derivar `nodeId`: SHA-256 da chave pública → primeiros 8 bytes → hex maiúsculo
3. Solicitar apelido ao usuário (onboarding)
4. Persistir chave privada com `EncryptedSharedPreferences`
5. Persistir chave pública e nodeId em `SharedPreferences` comum
6. Nunca regenerar as chaves após a primeira inicialização

**Ao iniciar o app nas vezes seguintes:**
- Carregar identidade das preferências, não gerar novamente

### `CryptoManager`

Responsabilidade: cifrar e decifrar payloads de mensagens.

**Cifrar (remetente):**
```
1. Receber: texto em claro (String) + chave pública do destinatário (ByteArray)
2. Derivar chave compartilhada via ECDH (chave privada local + chave pública do destinatário)
3. Derivar chave AES-256 via HKDF (SHA-256, salt aleatório de 32 bytes, info="meshpriv-v1")
4. Cifrar com AES-GCM: IV aleatório de 12 bytes + ciphertext + tag de 16 bytes
5. Retornar: salt (32) + iv (12) + ciphertext + tag — concatenados como ByteArray
```

**Decifrar (destinatário):**
```
1. Receber: ByteArray concatenado + chave pública do remetente (do MeshPacket)
2. Separar salt (32) + iv (12) + ciphertext+tag (resto)
3. Derivar chave compartilhada via ECDH (chave privada local + chave pública do remetente)
4. Derivar chave AES-256 via HKDF com mesmo salt
5. Decifrar com AES-GCM
6. Retornar texto em claro (String)
```

**Restrições:**
- Nunca logar bytes de chave privada, mesmo em debug
- Nunca reutilizar IV — sempre gerar aleatoriamente
- Nunca transmitir a chave privada na rede em hipótese alguma

---

## 8. Coleta de métricas — comportamento esperado

### `MetricsCollector`

Este é o componente mais crítico do projeto — os dados que ele coleta são o experimento do artigo.

**Eventos a registrar:**

| Evento | Campos |
|---|---|
| Mensagem enviada | messageId, sourceId, destinationId, sentAt, batteryLevelStart, networkSize |
| Mensagem entregue (destinatário) | messageId, receivedAt, hopCount, batteryLevelEnd |
| ACK recebido (remetente) | messageId, receivedAt do ACK, hopCount do ACK, batteryLevelEnd — fecha delivered=true |
| Mensagem descartada (TTL=0) | messageId, hopCount atingido |
| Peer conectado | peerId, connectedAt |
| Peer desconectado | peerId, disconnectedAt |

**Cálculo de latência (semântica revisada com o ACK):**
- **Lado do remetente:** `latencyMs = chegada_do_ACK - sentAt`, medido só pelo relógio
  local — sem o problema de clock sync, mas inclui a viagem de volta do ACK
  ("tempo até a confirmação", aproximadamente round-trip, não latência unidirecional)
- **Lado do destinatário:** o registro unidirecional `receivedAt - sentAt` continua
  sendo gravado, mas só é válido quando remetente e destinatário estão no mesmo
  teste sincronizado — registrar a limitação de clock sync no artigo
- `delivered=true` agora é fechado no remetente quando o ACK retorna — a taxa de
  entrega passa a ser mensurável no próprio dispositivo que enviou

**`BatteryMonitor`:**
```kotlin
fun getCurrentLevel(): Int {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    return if (level >= 0 && scale > 0) (level * 100 / scale) else -1
}
```

### `ExportMetricsCsvUseCase`

**Formato do CSV:**
```
metricId,messageId,sourceId,destinationId,latencyMs,hopCount,delivered,batteryStart,batteryEnd,networkSize,recordedAt
```

- Salvar em `context.getExternalFilesDir(null)/meshpriv_metrics_YYYYMMDD_HHmmss.csv`
- Compartilhar via `FileProvider` com `Intent.ACTION_SEND`
- Encoding: UTF-8

---

## 9. Banco de dados Room

### Versão inicial: 1

### Entidades

**`MessageEntity`**
```kotlin
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val senderId: String,
    val recipientId: String,
    val content: String,         // texto em claro (apenas se for destinatário)
    val sentAt: Long,
    val receivedAt: Long?,
    val hopCount: Int,
    val status: String           // "SENDING" | "DELIVERED" | "FAILED"
)
```

**`PeerEntity`**
```kotlin
@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val nodeId: String,
    val nickname: String,
    val publicKey: ByteArray,
    val endpointId: String,
    val signalStrength: Int,
    val lastSeenAt: Long,
    val isConnected: Boolean
)
```

**`MetricEntity`**
```kotlin
@Entity(tableName = "metrics")
data class MetricEntity(
    @PrimaryKey val metricId: String,
    val messageId: String,
    val sourceId: String,
    val destinationId: String,
    val latencyMs: Long,
    val hopCount: Int,
    val delivered: Boolean,
    val batteryLevelStart: Int,
    val batteryLevelEnd: Int,
    val networkSize: Int,
    val recordedAt: Long
)
```

### TypeConverters
- `ByteArray` ↔ `String` (Base64) para `publicKey`
- `Long?` para campos nullable de timestamp

---

## 10. Camada de UI — telas e estados

### Telas obrigatórias

**`HomeScreen`** — tela inicial
- Exibir: nodeId local (8 chars) e apelido
- Exibir: número de peers conectados
- Botão para ir para PeersScreen
- Botão para ir para MetricsScreen
- Status da rede (conectado / buscando peers)

**`PeersScreen`** — lista de peers descobertos
- Lista de `PeerCard` com: apelido, nodeId (4 chars), sinal, status
- Tap em um peer abre o `ChatScreen` para aquele peer
- Atualização em tempo real via StateFlow

**`ChatScreen`** — conversa com um peer
- Header: apelido + nodeId do peer
- Lista de mensagens com `MessageBubble` (enviadas à direita, recebidas à esquerda)
- Campo de texto + botão enviar
- Indicador de hop count ao lado de cada mensagem recebida
- Status de entrega (✓ enviada, ✓✓ entregue, ✗ falhou)

**`MetricsScreen`** — painel de métricas
- Total de mensagens enviadas / recebidas / com falha
- Latência média, mínima, máxima (em ms)
- Hop count médio
- Estimativa de consumo de bateria no período
- Botão "Exportar CSV"

### Padrão de estado nos ViewModels

```kotlin
// Sempre usar este padrão:
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val peer: Peer? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

private val _uiState = MutableStateFlow(ChatUiState())
val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
```

---

## 11. Permissões no AndroidManifest.xml

Estas permissões são obrigatórias para a Nearby Connections API funcionar:

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

Solicitar permissões em runtime antes de iniciar a descoberta.
Usar `rememberLauncherForActivityResult` com `ActivityResultContracts.RequestMultiplePermissions()`.

---

## 12. Dependências no `libs.versions.toml`

```toml
[versions]
kotlin = "2.0.21"
agp = "8.7.0"
compose-bom = "2024.11.00"
hilt = "2.52"
room = "2.6.1"
nearby = "19.1.0"
tink = "1.15.0"
coroutines = "1.9.0"
serialization = "1.7.3"
navigation-compose = "2.8.4"
lifecycle = "2.8.7"
turbine = "1.2.0"
mockk = "1.13.13"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Nearby Connections
nearby = { group = "com.google.android.gms", name = "play-services-nearby", version.ref = "nearby" }

# Tink (criptografia)
tink-android = { group = "com.google.crypto.tink", name = "tink-android", version.ref = "tink" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Serialization
serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation-compose" }

# Lifecycle
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }

# Testes
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
```

---

## 13. Convenções de código

### Nomenclatura
- Classes: `PascalCase`
- Funções e variáveis: `camelCase`
- Constantes: `SCREAMING_SNAKE_CASE` em `companion object`
- Arquivos: um arquivo por classe pública (mesmo nome)
- Rotas de navegação: `sealed class` em `AppNavGraph.kt`

### Kotlin idiomático
- Preferir `val` sobre `var` sempre que possível
- Usar `data class` para modelos
- Usar `sealed class` para estados de UI e resultados de operações
- Usar `when` exaustivo (sem `else` em sealed classes)
- Usar scope functions (`let`, `run`, `apply`, `also`, `with`) apropriadamente
- Sem `!!` (non-null assertion) — usar `?.` e `?:` ou `requireNotNull` com mensagem

### Coroutines
- Lançar coroutines em `viewModelScope` nos ViewModels
- Lançar coroutines em `CoroutineScope(Dispatchers.IO)` injetado nos repositórios
- Usar `withContext(Dispatchers.IO)` para operações de I/O dentro de suspend functions
- Nunca usar `GlobalScope`

### Compose
- Composables stateless sempre que possível (receber estado e callbacks como parâmetros)
- Extrair lógica de estado para o ViewModel — nunca em composables
- Nomear composables com substantivo descritivo: `MessageBubble`, `PeerCard`
- Preview de todo composable que não depende de ViewModel

### Comentários
- Comentar o "por quê", não o "o quê" (o código já diz o quê)
- Comentar decisões de segurança e criptografia explicitamente
- Comentar limitações conhecidas da Nearby Connections API quando relevante
- Usar `// TODO(semana-N):` para marcar o que foi adiado propositalmente

---

## 14. Convenções de commit

```
feat(mesh): implementa descoberta de peers via Nearby Connections
fix(crypto): corrige geração de IV para AES-GCM
test(router): adiciona testes de deduplicação por Message ID
docs(metrics): documenta formato do CSV exportado
refactor(ui): extrai MessageBubble para componente reutilizável
```

Prefixos: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`
Escopo entre parênteses: `mesh`, `crypto`, `ui`, `metrics`, `router`, `db`

---

## 15. Estratégia de testes

### O que testar com prioridade

| Componente | Tipo de teste | Prioridade |
|---|---|---|
| `MessageRouter` (deduplicação, TTL, flooding) | Unitário | Alta |
| `CryptoManager` (cifrar → decifrar roundtrip) | Unitário | Alta |
| `SeenMessageCache` (expiração, limite de tamanho) | Unitário | Alta |
| `ExportMetricsCsvUseCase` | Unitário | Média |
| `MetricsCollector` | Unitário com MockK | Média |
| `ChatViewModel` | Unitário com Turbine | Média |
| `NearbyConnectionsManager` | Nenhum (depende do hardware) | — |

### Padrão de teste de ViewModel com Turbine

```kotlin
@Test
fun `enviar mensagem atualiza estado para DELIVERING`() = runTest {
    viewModel.uiState.test {
        viewModel.sendMessage("Oi Maria")
        val state = awaitItem()
        assertThat(state.messages.last().status).isEqualTo(MessageStatus.SENDING)
    }
}
```

---

## 16. Cenário de experimento (para o artigo)

O experimento padrão usa **3 dispositivos físicos**:

```
Dispositivo A (Rafael) ←→ Dispositivo B (João) ←→ Dispositivo C (Maria)

A e C não se alcançam diretamente.
B atua como nó relay.
```

**O que medir:**
1. Latência de entrega A→C passando por B (múltiplas mensagens, calcular média/desvio)
2. Número de saltos confirmados (deve ser 2 para A→C)
3. Taxa de entrega (mensagens recebidas / mensagens enviadas)
4. Consumo de bateria nos três dispositivos durante 10 minutos de teste

**Variáveis do experimento:**
- Distância entre dispositivos (5m, 10m, 20m)
- Tamanho do payload (50, 200, 500 caracteres)
- TTL inicial (3, 5, 7)

Cada configuração deve ser repetida pelo menos 3 vezes para análise estatística.

---

## 17. Restrições absolutas — nunca violar

1. **Nunca transmitir a chave privada na rede** — nem em debug, nem em log, nem em nenhuma serialização
2. **Nunca usar Wi-Fi Aware** — suporte fragmentado causa falha silenciosa nos testes
3. **Nunca chamar servidor externo** — qualquer chamada HTTP viola o pressuposto do artigo
4. **Nunca usar `runBlocking` fora de testes** — causa ANR
5. **Nunca usar `GlobalScope`** — causa vazamento de memória
6. **Nunca reutilizar IV no AES-GCM** — quebra a segurança da criptografia
7. **Nunca usar `fallbackToDestructiveMigration`** — apagaria dados de experimentos
8. **Nunca implementar features fora do escopo do MVP** — o prazo é 20 de julho

---

## 18. O que fazer quando travar

Se a Nearby Connections API não estiver funcionando nos dispositivos de teste:
→ Verificar se o Google Play Services está atualizado nos três dispositivos
→ Verificar se as permissões de localização estão concedidas (obrigatório para Nearby)
→ Testar primeiro com um exemplo mínimo isolado antes de integrar ao projeto

Se a criptografia Tink der problema de compatibilidade:
→ Fallback: usar `javax.crypto` diretamente com ECDH + AES-GCM sem Tink
→ A lógica criptográfica deve estar isolada em `CryptoManager` para facilitar a troca

Se a exportação de CSV falhar:
→ Verificar configuração do `FileProvider` no `AndroidManifest.xml`
→ Verificar se `file_paths.xml` aponta para o diretório correto

---

*Última atualização: junho de 2026 — MeshPriv MVP para Latin.Science 2026*
