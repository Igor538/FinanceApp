# FinanceApp 💳📱

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%2520Compose-Material%25203-blue.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-API%2524%2B-green.svg?style=flat&logo=android)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Academic-orange.svg)]()

Aplicativo Android de controle financeiro desenvolvido com arquitetura moderna (**MVVM**, **Jetpack Compose**, **StateFlow**) conectado a uma API REST em **PHP/MySQL**.

[Repositório no GitHub](https://github.com/Igor538/FinanceApp)

</div>

---

## 📱 Sobre o Projeto

O **FinanceApp** é um sistema completo de gestão financeira pessoal. O aplicativo mobile consome uma API backend em PHP que interage com um banco de dados relacional MySQL. O fluxo de dados segue a arquitetura:

$$\text{Android App (Jetpack Compose)} \xrightarrow{\text{Retrofit / HTTP}} \text{API PHP (REST)} \xrightarrow{\text{PDO}} \text{MySQL Database}$$

---

## ✨ Funcionalidades

- 🔐 **Autenticação Segura:** Cadastro e login com hash de senha (`password_hash`).
- 📊 **Dashboard Financeiro:** Resumo de saldo, receitas, despesas e transações recentes em tempo real.
- 💸 **CRUD Completo:** Gerenciamento intuitivo de Transações, Contas e Categorias.
- 🎨 **UI Moderna & Adaptativa:** Desenvolvido 100% em Jetpack Compose com Material 3 e suporte a Tema Claro/Escuro persistente.
- ⚡ **Gerenciamento de Estado:** Utilização de `ViewModel`, `StateFlow` e corrotinas Kotlin.

---

## 🛠️ Tecnologias Utilizadas

- **Mobile:**
  - Kotlin 2.0+
  - Jetpack Compose & Material 3
  - Android Architecture Components (ViewModel, StateFlow, Lifecycle)
  - Navigation Compose
  - Retrofit + Gson (Requisições HTTP)
  - Preferences DataStore (Preferências e Tema)
- **Backend & Servidor:**
  - PHP 8.1+ com PDO
  - MySQL / MariaDB (compatível com WAMP ou XAMPP)

---

## 🚀 Como Executar o Projeto

Siga os passos abaixo para configurar o ambiente de desenvolvimento e executar a aplicação em sua máquina.

### 1. Configuração do Backend (Servidor Local)

Você pode utilizar **WAMP** ou **XAMPP**:

#### Opção A: Usando WAMP Server
1. Inicie os serviços **Apache** e **MySQL** no WAMP.
2. No phpMyAdmin (`http://localhost/phpmyadmin/`), crie um banco de dados chamado `financeapp`.
3. Importe o script SQL localizado em [`backend/database/financeapp.sql`](backend/database/financeapp.sql).
4. Copie a pasta [`backend/api`](backend/api) para `C:\wamp64\www\financeapp_api`.

#### Opção B: Usando XAMPP
1. Inicie **Apache** e **MySQL** no XAMPP Control Panel.
2. Crie o banco `financeapp` via phpMyAdmin e importe o arquivo [`backend/database/financeapp.sql`](backend/database/financeapp.sql).
3. Copie a pasta [`backend/api`](backend/api) para `C:\xampp\htdocs\finance_api`.

---

### 2. Configuração do Endereço da API no App Android

Abra o arquivo [`Api.kt`](app/src/main/java/com/example/financeapp/data/Api.kt) e configure a constante `BASE_URL` conforme o seu ambiente:

- **Emulador do Android Studio (WAMP):**
  ```kotlin
  const val BASE_URL = "http://10.0.2.2/financeapp_api/"
  ```
- **Emulador do Android Studio (XAMPP):**
  ```kotlin
  const val BASE_URL = "http://10.0.2.2/finance_api/"
  ```
- **Celular Físico (Mesma rede Wi-Fi):**
  Substitua `10.0.2.2` pelo IP local da sua máquina (ex: `http://192.168.0.10/financeapp_api/`).

> **Nota sobre caminhos no Windows:**
> Caso seu projeto esteja em um diretório com espaços ou acentos (ex: `Área de Trabalho`), o arquivo [`gradle.properties`](gradle.properties) já está configurado com `android.overridePathCheck=true` e o diretório temporário do Kotlin Daemon otimizado para evitar falhas de compilação.

---

### 3. Executando no Android Studio

1. Abra o Android Studio e selecione **Open**, escolhendo a pasta raiz do projeto (`FinanceApp`).
2. Aguarde o Gradle realizar a sincronização automática (`Sync Project with Gradle Files`).
3. Conecte um dispositivo Android via USB (com depuração ativada) ou selecione um Emulador AVD.
4. Clique no botão **Run ▶️** (`Shift + F10`).

---

## 📦 Gerando o APK

Para gerar o arquivo executável de instalação (`.apk`):
1. No menu superior do Android Studio, vá em: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. O APK de debug estará disponível em: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📂 Estrutura do Repositório

```text
FinanceApp/
├── app/                         # Aplicativo Android (Kotlin & Compose)
│   └── src/main/java/...        # Camadas UI, ViewModels, Modelos e Retrofit
├── backend/
│   ├── api/                     # Endpoints da API PHP (api.php, config.php)
│   └── database/                # Script SQL (financeapp.sql)
├── docs/                        # Documentação e assets do projeto
├── build.gradle.kts             # Configurações globais do Gradle
├── settings.gradle.kts          # Definição de módulos
└── README.md                    # Documentação oficial
```

---

## 👤 Autor

Desenvolvido por **Igor** ([Igor538](https://github.com/Igor538)).

---
*Projeto desenvolvido para fins educacionais e acadêmicos.*
