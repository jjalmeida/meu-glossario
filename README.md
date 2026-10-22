# Meu Glossário — Kotlin Multiplatform

Aplicativo para criar e organizar glossários e verbetes, com pesquisa avançada,
tags, favoritos, estatísticas, exportação/importação JSON e backup no Google
Drive. Agora é **Kotlin Multiplatform + Compose Multiplatform**, rodando em:

- **Android**
- **Desktop (JVM)** — Windows/macOS/Linux
- **Web (WebAssembly)** — navegador do computador

Toda a lógica (domínio, dados, casos de uso, ViewModels) e a **UI Compose** são
compartilhadas em `commonMain`. O que é específico de plataforma fica isolado
atrás de interfaces com `expect/actual`.

---

## Arquitetura

Clean Architecture + MVVM, com DI via **Koin** (comum + por plataforma):

```
commonMain
├─ domain/            modelos (@Serializable), repositórios (interfaces), casos de uso
├─ data/
│  ├─ local/          KeyValueStore (expect), LocalDataSource (estado + StateFlow)
│  ├─ repository/     repositórios em memória persistidos via KeyValueStore
│  ├─ dto/ mapper/    JSON com kotlinx.serialization
│  └─ prefs/          preferências de backup
├─ platform/          ExportService, BackupService, PlatformCapabilities (interfaces)
├─ di/                módulo Koin comum + expect platformModule
└─ ui/                tema, navegação, telas e componentes Compose + ViewModels

androidMain  → Room? não. Persistência via SharedPreferences; PDF (PdfDocument),
               compartilhamento (Intents), Drive (Google Sign-In + Drive API).
desktopMain  → persistência em arquivo; salvar via diálogo; Drive (OAuth app instalado).
wasmJsMain   → persistência em localStorage; download/upload de arquivo; Drive (GIS + REST).
```

### Persistência (memória + localStorage)

Não há banco relacional. O estado completo (glossários, verbetes, tags) é
serializado com `kotlinx.serialization` e guardado sob uma chave no
`KeyValueStore`:

- **Web:** `localStorage`
- **Desktop:** `~/.meuglossario/store.properties`
- **Android:** `SharedPreferences`

Os repositórios mantêm um cache reativo em memória (`StateFlow`) e persistem a
cada alteração.

---

## Matriz de recursos por plataforma

| Recurso                     | Android | Desktop | Web |
|-----------------------------|:------:|:------:|:---:|
| CRUD, busca, tags, favoritos, estatísticas | ✅ | ✅ | ✅ |
| Exportar/Importar JSON      | ✅ | ✅ | ✅ |
| Exportar PDF                | ✅ | ❌ | ❌ |
| Compartilhamento nativo     | ✅ | copiar p/ área de transferência | copiar p/ área de transferência |
| Backup Google Drive         | ✅ | ✅ | ✅ |

PDF e compartilhamento nativo ficam desabilitados fora do Android (a UI oculta
as ações não suportadas via `PlatformCapabilities`).

---

## Requisitos

- **JDK 17**
- **Android SDK** (API 35) para o alvo Android
- Navegador com **WebAssembly GC** (Chrome/Edge/Firefox recentes) para a web

---

## Configuração de segredos (ao clonar)

Este repositório **não** contém chaves, senhas ou credenciais — todos os
arquivos sensíveis estão no `.gitignore`. Depois de clonar, você precisa
fornecer os seus próprios para buildar os recursos que dependem deles.

| Segredo / arquivo | Para quê | Onde colocar | Obrigatório? |
|---|---|---|---|
| `keystore/meuglossario-upload.jks` + `keystore/keystore.properties` | Assinar o APK/AAB de **release** do Android | Raiz do projeto, pasta `keystore/` | Só para release Android |
| `client_secret.json` (OAuth tipo *Desktop*) | Backup no Drive no **Desktop** | `~/.meuglossario/client_secret.json` | Só p/ Drive no desktop |
| OAuth Client ID (tipo *Web*) | Backup no Drive na **Web** | Constante `CLIENT_ID` em `composeApp/src/wasmJsMain/resources/driveBridge.js` | Só p/ Drive na web |
| OAuth Client ID (tipo *Android*) | Backup no Drive no **Android** | Google Cloud Console (package + SHA-1); nada no código | Só p/ Drive no Android |
| Secret do GitHub `CLOUDFLARE_API_TOKEN` | Deploy automático da web | GitHub → Settings → Secrets and variables → Actions | Só p/ CI de deploy |
| Secret do GitHub `CLOUDFLARE_ACCOUNT_ID` | Deploy automático da web | GitHub → Settings → Secrets and variables → Actions | Só p/ CI de deploy |

> O app compila e roda sem nenhum desses segredos — apenas os recursos
> correspondentes (release assinado, backup no Drive, deploy automático) ficam
> indisponíveis até você configurá-los.

> ⚠️ **Nunca** faça commit da keystore, do `keystore.properties`, de qualquer
> `client_secret.json` ou de arquivos `.env`. O `.gitignore` já bloqueia esses
> padrões; mantenha-os assim.

Detalhes do Drive estão em [Configuração do Google Drive](#configuração-do-google-drive);
detalhes da assinatura Android em [Assinatura e publicação no Google Play](#assinatura-e-publicação-no-google-play-android).

---

## Como executar

### Web (navegador do computador)

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Abre em `http://localhost:8080`. Para gerar os arquivos estáticos de produção:

```bash
./gradlew :composeApp:wasmJsBrowserDistribution
# saída em composeApp/build/dist/wasmJs/productionExecutable
```

### Publicar a web no Cloudflare Pages (grátis)

A versão web é 100% estática. O build gera a pasta
`composeApp/build/dist/wasmJs/productionExecutable/` (com `_headers` e
`_redirects` já incluídos para MIME do `.wasm` e roteamento SPA).

**Opção A — Deploy por CLI (wrangler):**

```bash
# 1) Instale o wrangler e faça login
npm install -g wrangler
wrangler login

# 2) Gere a distribuição de produção
./gradlew :composeApp:wasmJsBrowserDistribution

# 3) Publique (cria o projeto "meu-glossario" na primeira vez)
wrangler pages deploy composeApp/build/dist/wasmJs/productionExecutable --project-name=meu-glossario
```

O site fica em `https://meu-glossario.pages.dev`.

**Opção B — Deploy automático via GitHub (CI):**

1. Suba o repositório no GitHub.
2. No Cloudflare, crie um **API Token** (permissão *Cloudflare Pages: Edit*) e
   copie o **Account ID**.
3. No GitHub, adicione os secrets `CLOUDFLARE_API_TOKEN` e `CLOUDFLARE_ACCOUNT_ID`.
4. Cada push na `main` publica automaticamente (workflow em
   `.github/workflows/deploy-web.yml`).

**Opção C — Painel do Cloudflare (Git connect):**

- Conecte o repositório em Pages e configure:
  - Build command: `./gradlew :composeApp:wasmJsBrowserDistribution`
  - Output directory: `composeApp/build/dist/wasmJs/productionExecutable`

**Google Drive em produção (web):** no Google Cloud Console, adicione a origem
`https://meu-glossario.pages.dev` (e seu domínio customizado, se houver) nas
*Authorized JavaScript origins* do OAuth Client, e informe o `CLIENT_ID` em
`composeApp/src/wasmJsMain/resources/driveBridge.js`. HTTPS é obrigatório (o
Pages já fornece).

### Desktop

```bash
./gradlew :composeApp:run
```

Empacotar instalador nativo (msi/dmg/deb):

```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

### Android

Abra no Android Studio e rode a configuração do módulo `composeApp`, ou:

```bash
./gradlew :composeApp:installDebug
```

### Testes (comuns)

```bash
./gradlew :composeApp:desktopTest
```

Os testes ficam em `commonTest` e exercitam casos de uso e repositórios reais
sobre um `KeyValueStore` em memória.

> **Windows + OneDrive:** se o projeto estiver dentro de uma pasta sincronizada
> pelo OneDrive, o sincronismo pode travar arquivos em `build/` durante a
> compilação. Redirecione a saída de build para fora do OneDrive:
> ```bash
> ./gradlew -PbuildOutDir=C:/tmp/mg-build :composeApp:desktopTest
> ```

---

## Assinatura e publicação no Google Play (Android)

O build de **release** do Android é assinado com uma *upload key* (keystore).
Por segurança, a keystore e suas senhas ficam fora do repositório: o
`build.gradle.kts` lê as credenciais de `keystore/keystore.properties`.

### 1. Gerar a keystore (uma única vez)

```bash
keytool -genkeypair -v \
  -keystore keystore/meuglossario-upload.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias meuglossario
```

Guarde o arquivo `.jks` e a senha em local seguro (gerenciador de senhas).
**Se perder a keystore, não será possível atualizar o app publicado.**

### 2. Criar `keystore/keystore.properties`

```properties
storeFile=keystore/meuglossario-upload.jks
storePassword=SUA_SENHA_DA_STORE
keyAlias=meuglossario
keyPassword=SUA_SENHA_DA_KEY
```

> `storeFile` é relativo à raiz do projeto. Tanto o `.jks` quanto o
> `.properties` já estão no `.gitignore` e não devem ser versionados.

### 3. Gerar o Android App Bundle (`.aab`)

```bash
./gradlew :composeApp:bundleRelease
# saída: composeApp/build/outputs/bundle/release/composeApp-release.aab
```

Se os arquivos de keystore não existirem, o build de release ainda funciona,
porém **sem assinatura** (não serve para a Play Store).

### 4. Publicar

1. Crie uma conta no [Google Play Console](https://play.google.com/console)
   (taxa única de US$ 25).
2. Crie o app, preencha a ficha da loja e os questionários obrigatórios
   (classificação, público-alvo, segurança de dados).
3. Em **Produção** (ou **Teste interno**, recomendado primeiro), crie um release
   e faça upload do `.aab`.
4. Aceite o **Play App Signing**: a keystore acima vira sua *upload key* e o
   Google gerencia a chave de distribuição final.

> **Windows + pasta sincronizada (OneDrive):** se o build travar por lock de
> arquivo em `build/`, redirecione a saída:
> `./gradlew -PbuildOutDir=C:/tmp/mg-build :composeApp:bundleRelease`.

---

## Configuração do Google Drive

O backup usa a pasta oculta do app (`appDataFolder`).

- **Android:** crie um OAuth Client ID do tipo **Android** (package `com.meuglossario`
  + SHA-1 da sua chave) no Google Cloud Console, com a Drive API habilitada.
- **Desktop:** crie um OAuth Client ID do tipo **Desktop**, baixe o
  `client_secret.json` e coloque em `~/.meuglossario/client_secret.json`. O
  primeiro backup abrirá o navegador para autorizar (porta local 8888).
- **Web:** crie um OAuth Client ID do tipo **Web** e informe-o em
  `composeApp/src/wasmJsMain/resources/driveBridge.js` (constante `CLIENT_ID`).
  Adicione a origem do site nas "Authorized JavaScript origins".

---

## Formato do JSON

```json
{
  "nome": "Banco de Dados",
  "dataCriacao": "2026-01-01",
  "verbetes": [
    {
      "palavra": "Index",
      "definicao": "Estrutura que acelera consultas",
      "observacao": "",
      "referencia": "Documentação PostgreSQL, cap. 11",
      "favorito": true,
      "tags": ["PostgreSQL"]
    }
  ]
}
```

O backup do Drive agrupa vários glossários em
`{ "versao", "dataBackup", "glossarios": [ ... ] }`.
