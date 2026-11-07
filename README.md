<div align="center">

# FinanceApp 💳📱

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/jetpack/compose)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-API%2036-3DDC84?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com)
[![Gradle](https://img.shields.io/badge/Gradle-8.13-02303A?logo=gradle&logoColor=white&style=for-the-badge)](https://gradle.org)
[![PHP](https://img.shields.io/badge/PHP-8.1+-777BB4?logo=php&logoColor=white&style=for-the-badge)](https://www.php.net)
[![MySQL](https://img.shields.io/badge/MySQL-MariaDB-4479A1?logo=mysql&logoColor=white&style=for-the-badge)](https://www.mysql.com)

<br/>

> **Aplicativo Android de controle financeiro pessoal** com arquitetura moderna (**MVVM**, **Jetpack Compose**, **StateFlow**) conectado a uma API REST em **PHP/MySQL**.

</div>

---

## 📱 Sobre o Projeto

O **FinanceApp** é um sistema completo de gestão financeira pessoal. O aplicativo mobile consome uma API backend em PHP que interage com um banco de dados relacional MySQL.

---

## 🔑 Conta de Teste / Demonstração

O script SQL já insere uma conta de demonstração pré-configurada com contas, categorias e transações de exemplo. Além disso, a tela de login possui um card com botão de preenchimento automático:

<table>
  <tr>
    <td><b>Campo</b></td>
    <td><b>Valor</b></td>
  </tr>
  <tr>
    <td>Usuário</td>
    <td><code>admin</code></td>
  </tr>
  <tr>
    <td>Senha</td>
    <td><code>123456</code></td>
  </tr>
</table>

---

## ✨ Funcionalidades

<div align="left">

| Funcionalidade | Descrição |
|---|---|
| 🔐 **Autenticação** | Cadastro e login com hash de senha (`password_hash` no PHP) |
| 📊 **Dashboard** | Resumo de saldo consolidado, receitas, despesas e transações recentes |
| 💸 **Transações** | Criar, editar e excluir movimentações financeiras |
| 🏦 **Contas** | Gerenciamento de contas bancárias/carteiras com saldo |
| 🏷️ **Categorias** | Categorias de receita e despesa |
| 🎨 **UI Moderna** | 100% Jetpack Compose com Material 3 e tema claro/escuro persistente |
| ⚡ **Estado** | `ViewModel`, `StateFlow` e corrotinas Kotlin |
| 💾 **Preferências** | Tema e ID do usuário salvos localmente com DataStore |

</div>

---

## 🛠️ Tecnologias Utilizadas

### Mobile (Android)

<table>
  <tr>
    <td><b>Tecnologia</b></td>
    <td><b>Versão</b></td>
    <td><b>Uso</b></td>
  </tr>
  <tr>
    <td>Kotlin</td>
    <td>2.0.21</td>
    <td>Linguagem principal</td>
  </tr>
  <tr>
    <td>Jetpack Compose & Material 3</td>
    <td>—</td>
    <td>Interface de usuário moderna</td>
  </tr>
  <tr>
    <td>Navigation Compose</td>
    <td>2.8.5</td>
    <td>Navegação entre telas</td>
  </tr>
  <tr>
    <td>ViewModel / StateFlow / Lifecycle</td>
    <td>—</td>
    <td>Gerenciamento de estado e ciclo de vida</td>
  </tr>
  <tr>
    <td>Retrofit + Gson</td>
    <td>2.11.0</td>
    <td>Requisições HTTP para a API</td>
  </tr>
  <tr>
    <td>OkHttp</td>
    <td>4.12.0</td>
    <td>Cliente HTTP e logging interceptor</td>
  </tr>
  <tr>
    <td>Preferences DataStore</td>
    <td>1.1.1</td>
    <td>Preferências locais (tema, usuário)</td>
  </tr>
  <tr>
    <td>Kotlin Coroutines</td>
    <td>1.9.0</td>
    <td>Programação assíncrona</td>
  </tr>
</table>

### Backend & Servidor

<table>
  <tr>
    <td><b>Tecnologia</b></td>
    <td><b>Uso</b></td>
  </tr>
  <tr>
    <td>PHP 8.1+ com PDO</td>
    <td>API REST e conexão com banco de dados</td>
  </tr>
  <tr>
    <td>MySQL / MariaDB</td>
    <td>Banco de dados relacional (WAMP ou XAMPP)</td>
  </tr>
</table>

### Ferramentas de Build

<table>
  <tr>
    <td><b>Ferramenta</b></td>
    <td><b>Versão</b></td>
  </tr>
  <tr>
    <td>Android Gradle Plugin</td>
    <td>8.13.2</td>
  </tr>
  <tr>
    <td>Gradle</td>
    <td>8.13</td>
  </tr>
  <tr>
    <td>compileSdk / targetSdk</td>
    <td>36</td>
  </tr>
  <tr>
    <td>minSdk</td>
    <td>24</td>
  </tr>
</table>

---

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

<div class="req-list">

| Ferramenta | Versão recomendada | Observação |
|---|---|---|
| **Android Studio** | Iguana ou superior | IDE para desenvolvimento Android |
| **Android SDK** | — | Build-tools e platform-tools |
| **Java JDK** | 21 (Eclipse Temurin 21.0.13) | Necessário para o Gradle |
| **Gradle** | 8.13 | Via wrapper `./gradlew` (já incluído) |
| **Emulador Android** | AVD ou dispositivo físico | Com depuração USB ativada |
| **WAMP ou XAMPP** | — | Para rodar a API PHP e MySQL localmente |

</div>

> 💡 Os comandos apresentados nesta documentação são para **PowerShell no Windows**.

---

## ⚙️ Configuração do Ambiente (Windows)

### 1. Variável de ambiente `JAVA_HOME`

O Gradle precisa localizar o JDK 21. Configure no Windows:

1. Abra o **Painel de Controle > Sistema > Configurações avançadas do sistema > Variáveis de Ambiente**.
2. Em **Variáveis do sistema**, clique em **Novo** e adicione:
   - Nome da variável: `JAVA_HOME`
   - Valor da variável: caminho da sua instalação do JDK 21, por exemplo:
     ```
     C:\Program Files\Eclipse Adoptium\jdk-21.0.13+11-hotspot
     ```
     *(Ajuste o caminho conforme a versão instalada em sua máquina.)*
3. Ainda em **Variáveis do sistema**, localize a variável `Path`, selecione-a e clique em **Editar**. Adicione:
   ```
   %JAVA_HOME%\bin
   ```
4. Feche e reabra o terminal (PowerShell) para aplicar.

### 2. Verifique a instalação

No PowerShell:

```powershell
java -version
.\gradlew --version
```

O `java -version` deve reportar o **Java 21** e o `gradlew --version` deve usar essa mesma JVM.

> ⚠️ Se o Gradle estiver usando uma versão incorreta do Java, verifique se o `JAVA_HOME` aponta para o JDK 21 e se `%JAVA_HOME%\bin` está antes de outras instalações Java no `Path`.

---

## 🗄️ Configuração do Backend (API PHP + MySQL)

### Produção (alwaysdata)

A API de produção roda em `https://financeapp.alwaysdata.net/` (PHP + MariaDB + SSL grátis).

1. Crie a conta em https://www.alwaysdata.com (plano Free).
2. Em **Bases de données > MySQL**, crie o banco e importe `backend/database/financeapp.sql`.
3. Envie `backend/api/api.php` e `backend/api/config.php` para `www/`.
4. Crie `www/config.local.php` com as credenciais reais (modelo abaixo). Ele **não** vai para o git.
5. Teste: `https://CONTA.alwaysdata.net/api.php?action=accounts&user_id=1`.

Modelo de `config.local.php`:

```php
<?php
define('DB_HOST', 'mysql-CONTA.alwaysdata.net');
define('DB_PORT', '3306');
define('DB_NAME', 'CONTA_financeapp');
define('DB_USER', 'CONTA');
define('DB_PASS', 'SUA_SENHA');
```

### Desenvolvimento local (WAMP/XAMPP)

Você pode utilizar **WAMP** ou **XAMPP**:

### Opção A: WAMP Server

1. Inicie os serviços **Apache** e **MySQL** no WAMP.
2. No phpMyAdmin (`http://localhost/phpmyadmin/`), crie um banco de dados chamado **`transactions`**.
3. Importe o script SQL localizado em [`backend/database/financeapp.sql`](backend/database/financeapp.sql).
4. Copie a pasta [`backend/api`](backend/api) para `C:\wamp64\www\financeapp_api`.

### Opção B: XAMPP

1. Inicie **Apache** e **MySQL** no XAMPP Control Panel.
2. Crie o banco **`transactions`** via phpMyAdmin e importe [`backend/database/financeapp.sql`](backend/database/financeapp.sql).
3. Copie a pasta [`backend/api`](backend/api) para `C:\xampp\htdocs\finance_api`.

> 📌 **Nota sobre caminhos no Windows:** Caso o projeto esteja em um diretório com espaços ou acentos (ex: `Área de Trabalho`), o arquivo [`gradle.properties`](gradle.properties) já está configurado com `android.overridePathCheck=true` e o diretório temporário do Kotlin Daemon otimizado para evitar falhas de compilação.

---

## 📱 Configuração do Emulador Android

1. No Android Studio, abra o **Device Manager** (`View > Tool Windows > Device Manager`).
2. Clique em **Create device** e selecione um modelo (ex: Medium Phone).
3. Escolha uma imagem do sistema (ex: Android 17 / API 34 ou superior) e finalize a criação do AVD.
4. Inicie o emulador clicando no ícone de play ao lado do dispositivo criado.

### Verificar conexão com o ADB

No PowerShell, na raiz do projeto:

```powershell
adb devices
```

Você deve ver o emulador listado como `emulator-5554` (ou similar) com o status `device`.

> 🔄 Se o emulador não for reconhecido, tente reiniciá-lo pelo Device Manager e execute `adb devices` novamente.

---

## 🔧 Configuração da API no App Android

A constante `DEFAULT_BASE_URL` em [`app/src/main/java/com/example/financeapp/data/Api.kt`](app/src/main/java/com/example/financeapp/data/Api.kt) já aponta para a produção (`https://financeapp.alwaysdata.net/`). Para desenvolvimento local, altere essa constante no código e recompile o app.

<table>
  <tr>
    <td><b>Ambiente</b></td>
    <td><b>DEFAULT_BASE_URL</b></td>
  </tr>
  <tr>
    <td>Produção (alwaysdata)</td>
    <td><code>https://financeapp.alwaysdata.net/</code></td>
  </tr>
  <tr>
    <td>Emulador (WAMP)</td>
    <td><code>http://10.0.2.2/financeapp_api/</code></td>
  </tr>
  <tr>
    <td>Emulador (XAMPP)</td>
    <td><code>http://10.0.2.2/finance_api/</code></td>
  </tr>
  <tr>
    <td>Celular físico (mesma Wi-Fi)</td>
    <td><code>http://SEU_IP_LOCAL/financeapp_api/</code></td>
  </tr>
</table>

> 🌐 O endereço `10.0.2.2` é um alias especial do emulador Android que aponta para o `localhost` do computador host.

---

## ▶️ Execução do Projeto

Todos os comandos abaixo devem ser executados no **PowerShell**, na raiz do projeto (`FinanceApp`).

### 1. Verificar a versão do Gradle

```powershell
.\gradlew --version
```

### 2. Compilar o aplicativo (modo debug)

```powershell
.\gradlew assembleDebug
```

O APK será gerado em:

```
app/build/outputs/apk/debug/app-debug.apk
```

### 3. Instalar no emulador/dispositivo

```powershell
.\gradlew installDebug
```

Resultado esperado:

```text
BUILD SUCCESSFUL
Installed on 1 device.
```

### Alternativa: executar pelo Android Studio

1. Abra o Android Studio e selecione **Open**, escolhendo a pasta raiz do projeto (`FinanceApp`).
2. Aguarde a sincronização automática do Gradle (`Sync Project with Gradle Files`).
3. Conecte um dispositivo via USB (com depuração ativada) ou selecione um Emulador AVD.
4. Clique em **Run ▶️** (`Shift + F10`).

---

## 🧩 Solução de Problemas

<div class="troubleshoot">

| Problema | Solução |
|---|---|
| **Gradle utilizando Java incorreto** | Verifique se `JAVA_HOME` aponta para o JDK 21 e se `%JAVA_HOME%\bin` está no `Path` antes de outras instalações Java. Reinicie o PowerShell e rode `.\gradlew --version`. |
| **`JAVA_HOME` não configurado** | O Gradle falhará indicando que não encontrou o Java. Siga a seção **Configuração do Ambiente** acima. |
| **`No connected devices!`** | Confirme que o emulador está iniciado ou que o dispositivo físico está conectado com depuração USB ativada. Execute `adb devices` para verificar. |
| **Emulador não reconhecido pelo ADB** | Reinicie o emulador pelo Device Manager. Execute `adb devices` novamente. Se persistir, rode `adb kill-server` e depois `adb start-server`. |
| **Erro de conexão com a API** | Verifique sua internet e se o backend está no ar (`https://financeapp.alwaysdata.net/api.php?action=accounts&user_id=1`). Para dev local, confirme WAMP/XAMPP e a `DEFAULT_BASE_URL` em `Api.kt`. |
| **Celular físico: "sem permissão" / 403** | O Apache do WAMP bloqueia IPs externos por padrão. Libere: (1) em `wamp64\bin\apache\...\conf\extra\httpd-vhosts.conf`, dentro de `<Directory ...www/>`, adicione `Require ip 192.168.0.0/16` abaixo do `Require local`; (2) reinicie o Apache (ícone do WAMP → Reiniciar Serviços, ou `Restart-Service wampapache64` como Administrador); (3) libere a porta 80 no Firewall do Windows: `netsh advfirewall firewall add rule name="WAMP Apache 80" dir=in action=allow protocol=TCP localport=80`. |
| **Celular físico: "não foi possível conectar"** | Para dev local, confirme que celular e PC estão no **mesmo Wi-Fi**, pegue o IPv4 do PC com `ipconfig` e ajuste a `DEFAULT_BASE_URL` em `Api.kt` para `http://SEU-IP/financeapp_api/` (recompile). Teste antes no navegador do celular. |
| **Erro de permissão do firewall / UAC não aparece** | Abra o PowerShell como Administrador (Win+X → Terminal (Admin)) e rode o comando `netsh` acima manualmente. |

</div>

---

## 📂 Estrutura do Repositório

```text
FinanceApp/
├── app/                         # Aplicativo Android (Kotlin & Compose)
│   └── src/main/java/com/example/financeapp/
│       ├── MainActivity.kt      # Telas, navegação e ViewModel
│       ├── App.kt               # Classe Application
│       └── data/                # Camada de dados
│           ├── Api.kt           # Retrofit e BASE_URL
│           ├── FinanceRepository.kt
│           └── Preferences.kt   # DataStore
├── backend/
│   ├── api/                     # Endpoints PHP (api.php, config.php)
│   │   └── config.local.php     # Credenciais reais (ignorado pelo git)
│   └── database/                # Script SQL (financeapp.sql)
├── docs/                        # Documentação e assets
├── build.gradle.kts             # Configurações globais de build
├── settings.gradle.kts          # Módulos e repositórios
├── gradle.properties            # Propriedades do Gradle
├── gradle/
│   ├── libs.versions.toml       # Catálogo de versões e dependências
│   └── wrapper/                 # Wrapper do Gradle
├── local.properties             # SDK local (não versionar)
└── README.md                    # Documentação oficial
```

---

## 📥 Downloads (APK)

Instale diretamente no seu dispositivo Android (aplicativos de fontes desconhecidas permitido):

| Versão | Arquivo | Descrição |
|---|---|---|
| **v2.1** | [`apks/FinanceApp-v2.1.apk`](apks/FinanceApp-v2.1.apk) | Backend em nuvem (alwaysdata) — baixe e use direto |
| **v2.0** | [`apks/FinanceApp-v2.0.apk`](apks/FinanceApp-v2.0.apk) | Interface redesenhada (novo tema, cards centralizados, toggle de senha) |
| **v1.0** | [`apks/FinanceApp-v1.0.apk`](apks/FinanceApp-v1.0.apk) | Versão original do projeto |

> O código da v1.0 está disponível na branch [`v1.0`](https://github.com/Igor538/FinanceApp/tree/v1.0). A branch `main` contém a v2.0.

---

## 👤 Autor

Desenvolvido por **Igor** ([Igor538](https://github.com/Igor538)).
