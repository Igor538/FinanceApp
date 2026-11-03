package com.example.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.financeapp.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FinanceRoot() }
    }
}

class FinanceViewModel(private val repository: FinanceRepository, private val prefs: Preferences) :
    ViewModel() {
    private val _user = MutableStateFlow<UserDto?>(null);
    val user: StateFlow<UserDto?> = _user.asStateFlow()
    private val _transactions = MutableStateFlow<List<TransactionDto>>(emptyList());
    val transactions: StateFlow<List<TransactionDto>> = _transactions.asStateFlow()
    private val _accounts = MutableStateFlow<List<AccountDto>>(emptyList());
    val accounts: StateFlow<List<AccountDto>> = _accounts.asStateFlow()
    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList());
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()
    private val _dark = MutableStateFlow(false);
    val dark: StateFlow<Boolean> = _dark.asStateFlow()
    private val _message = MutableStateFlow("");
    val message: StateFlow<String> = _message.asStateFlow()
    private val _busy = MutableStateFlow(false);
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    init {
        viewModelScope.launch { _dark.value = prefs.darkTheme.first() }
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        if (username.isBlank() || password.isBlank()) {
            _message.value = "Informe usuário e senha."; return@launch
        }
        _busy.value = true
        try {
            val r = repository.login(username, password); if (r.success && r.user != null) {
                _user.value =
                    r.user; prefs.setUserId(r.user.id); loadAll(r.user.id); _message.value =
                    "Login realizado."
            } else _message.value = r.message
        } catch (e: Exception) {
            _message.value =
                "Não foi possível conectar à API. Confira o WAMP e o endereço do servidor."
        } finally {
            _busy.value = false
        }
    }

    fun register(name: String, username: String, password: String) = viewModelScope.launch {
        if (name.isBlank() || username.isBlank() || password.length < 6) {
            _message.value =
                "Preencha os campos. A senha deve ter ao menos 6 caracteres."; return@launch
        }
        try {
            val r = repository.register(name, username, password); _message.value = r.message
        } catch (_: Exception) {
            _message.value = "Falha ao cadastrar. Verifique a API."
        }
    }

    private suspend fun loadAll(id: Int) {
        try {
            _transactions.value = repository.transactions(id).data; _accounts.value =
                repository.accounts(id).data; _categories.value = repository.categories(id).data
        } catch (_: Exception) {
            _message.value = "Falha ao carregar dados. Verifique a API e o banco."
        }
    }

    fun reload() = viewModelScope.launch {
        _user.value?.let { loadAll(it.id) } ?: prefs.userId.first().takeIf { it > 0 }
            ?.let { loadAll(it) }
    }

    fun saveTransaction(
        id: Int,
        description: String,
        amount: Double,
        type: String,
        accountId: Int,
        categoryId: Int?,
        date: String
    ) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first()
        try {
            val body = mutableMapOf<String, Any>(
                "user_id" to uid,
                "description" to description,
                "amount" to amount,
                "type" to type,
                "account_id" to accountId,
                "transaction_date" to date
            )
            if (id > 0) body["id"] = id; if (categoryId != null) body["category_id"] = categoryId
            _message.value = repository.saveTransaction(body).message; loadAll(uid)
        } catch (_: Exception) {
            _message.value = "Erro ao salvar transação."
        }
    }

    fun deleteTransaction(id: Int) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first(); try {
        _message.value = repository.deleteTransaction(id, uid).message; loadAll(uid)
    } catch (_: Exception) {
        _message.value = "Erro ao excluir transação."
    }
    }

    fun saveAccount(id: Int, name: String, balance: Double) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first(); try {
        val b = mutableMapOf<String, Any>(
            "user_id" to uid,
            "name" to name,
            "balance" to balance
        ); if (id > 0) b["id"] = id; _message.value =
            repository.saveAccount(b).message; loadAll(uid)
    } catch (_: Exception) {
        _message.value = "Erro ao salvar conta."
    }
    }

    fun deleteAccount(id: Int) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first(); try {
        _message.value = repository.deleteAccount(id, uid).message; loadAll(uid)
    } catch (_: Exception) {
        _message.value = "Erro ao excluir conta."
    }
    }

    fun saveCategory(id: Int, name: String, type: String) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first(); try {
        val b = mutableMapOf<String, Any>(
            "user_id" to uid,
            "name" to name,
            "type" to type
        ); if (id > 0) b["id"] = id; _message.value = repository.saveCategory(b).message; loadAll(
            uid
        )
    } catch (_: Exception) {
        _message.value = "Erro ao salvar categoria."
    }
    }

    fun deleteCategory(id: Int) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first(); try {
        _message.value = repository.deleteCategory(id, uid).message; loadAll(uid)
    } catch (_: Exception) {
        _message.value = "Erro ao excluir categoria."
    }
    }

    fun toggleDark(value: Boolean) =
        viewModelScope.launch { _dark.value = value; prefs.setDarkTheme(value) }

    fun logout() = viewModelScope.launch {
        prefs.clearUser(); _user.value = null; _transactions.value = emptyList(); _accounts.value =
        emptyList(); _categories.value = emptyList()
    }

    fun consumeMessage() {
        _message.value = ""
    }
}

@Composable
fun FinanceRoot() {
    val context = LocalContext.current
    val vm: FinanceViewModel =
        viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FinanceViewModel(FinanceRepository(), Preferences(context)) as T
        })
    val dark by vm.dark.collectAsState()
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        val nav = rememberNavController()
        NavHost(navController = nav, startDestination = "login") {
            composable("login") { LoginScreen(vm, nav) }
            composable("home") { MainShell(vm, nav) }
        }
    }
}

@Composable
private fun LoginScreen(vm: FinanceViewModel, nav: NavHostController) {
    var username by remember { mutableStateOf("") };
    var password by remember { mutableStateOf("") };
    var name by remember { mutableStateOf("") };
    var registering by remember { mutableStateOf(false) }
    val busy by vm.busy.collectAsState();
    val message by vm.message.collectAsState();
    val user by vm.user.collectAsState()
    LaunchedEffect(user) {
        if (user != null) nav.navigate("home") {
            popUpTo("login") {
                inclusive = true
            }
        }
    }
    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            kotlinx.coroutines.delay(3500); vm.consumeMessage()
        }
    }
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "FinanceApp",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text("Organize sua vida financeira", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            if (registering) OutlinedTextField(
                name,
                { name = it },
                label = { Text("Nome completo") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                username,
                { username = it },
                label = { Text("Usuário") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                password,
                { password = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (registering) vm.register(name, username, password) else vm.login(
                        username,
                        password
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy
            ) { Text(if (busy) "Aguarde..." else if (registering) "Criar conta" else "Entrar") }
            TextButton(onClick = {
                registering = !registering
            }) { Text(if (registering) "Já tenho conta · Entrar" else "Não tem conta? Cadastre-se") }
            if (message.isNotBlank())
                Text(message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun MainShell(vm: FinanceViewModel, nav: NavHostController) {
    var tab by remember { mutableStateOf("Início") }
    val user by vm.user.collectAsState();
    val transactions by vm.transactions.collectAsState();
    val accounts by vm.accounts.collectAsState();
    val categories by vm.categories.collectAsState();
    val message by vm.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.reload() }
    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            snackbarHostState.showSnackbar(message); vm.consumeMessage()
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                listOf(
                    "Início",
                    "Transações",
                    "Contas",
                    "Categorias",
                    "Ajustes"
                ).forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = {
                            Text(
                                when (item) {
                                    "Início" -> "⌂"; "Transações" -> "↕"; "Contas" -> "▣"; "Categorias" -> "▤"; else -> "⚙"
                                }
                            )
                        },
                        label = { Text(item) })
                }
            }
        }) { padding ->
        when (tab) {
            "Início" -> DashboardScreen(
                user?.name ?: "Usuário",
                transactions,
                accounts,
                Modifier.padding(padding)
            )

            "Transações" -> TransactionsScreen(
                vm,
                transactions,
                accounts,
                categories,
                Modifier.padding(padding)
            )

            "Contas" -> AccountsScreen(vm, accounts, Modifier.padding(padding))
            "Categorias" -> CategoriesScreen(vm, categories, Modifier.padding(padding))
            else -> SettingsScreen(
                vm,
                user?.name ?: "Usuário",
                Modifier.padding(padding),
                onLogout = { vm.logout(); nav.navigate("login") { popUpTo(0) } })
        }
    }
}

private fun money(v: Double) = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(v)
@Composable
private fun DashboardScreen(
    name: String,
    tx: List<TransactionDto>,
    accounts: List<AccountDto>,
    modifier: Modifier = Modifier
) {
    val income = tx.filter { it.type == "income" }.sumOf { it.amount };
    val expense = tx.filter { it.type == "expense" }.sumOf { it.amount }
    Column(modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp)) {
        Text(
            "Olá, $name",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        ); Text(
        "Aqui está seu resumo financeiro",
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
        Spacer(Modifier.height(20.dp)); Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "Saldo consolidado",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ); Text(
            money(accounts.sumOf { it.balance }),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        }
    }
        Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SummaryCard(
            "Receitas",
            income,
            Modifier.weight(1f)
        ); SummaryCard("Despesas", expense, Modifier.weight(1f))
    }
        Spacer(Modifier.height(22.dp)); Text(
        "Movimentações recentes",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
        if (tx.isEmpty()) Text(
            "Nenhuma transação cadastrada.",
            Modifier.padding(vertical = 18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        tx.take(5).forEach { TransactionRow(it) }
    }
}

@Composable
private fun SummaryCard(label: String, value: Double, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ); Text(money(value), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TransactionRow(t: TransactionDto) {
    ListItem(
        headlineContent = { Text(t.description, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text("${t.transaction_date} · ${t.type}") },
        trailingContent = {
            Text(
                (if (t.type == "income") "+" else "−") + money(t.amount),
                fontWeight = FontWeight.Bold
            )
        })
}

@Composable
private fun TransactionsScreen(
    vm: FinanceViewModel,
    tx: List<TransactionDto>,
    accounts: List<AccountDto>,
    categories: List<CategoryDto>,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) };
    var editing by remember { mutableStateOf<TransactionDto?>(null) }
    Column(modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Transações",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            ); Button(onClick = { editing = null; show = true }) { Text("Adicionar") }
        }
        if (tx.isEmpty()) Text("Cadastre sua primeira movimentação.", Modifier.padding(16.dp))
        LazyColumn {
            items(tx, key = { it.id }) { t ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        TransactionRow(t); Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            editing = t; show = true
                        }) { Text("Editar") }; TextButton(
                        onClick = { vm.deleteTransaction(t.id) }) { Text("Excluir") }
                    }
                    }
                }
            }
        }
    }
    if (show) TransactionDialog(
        editing,
        accounts,
        categories,
        { d, a, v, typ, acc, cat, date ->
            vm.saveTransaction(d, a, v, typ, acc, cat, date); show = false
        },
        { show = false })
}

@Composable
private fun TransactionDialog(
    old: TransactionDto?,
    accounts: List<AccountDto>,
    categories: List<CategoryDto>,
    save: (Int, String, Double, String, Int, Int?, String) -> Unit,
    dismiss: () -> Unit
) {
    var desc by remember { mutableStateOf(old?.description ?: "") };
    var amount by remember { mutableStateOf(old?.amount?.toString() ?: "") };
    var type by remember { mutableStateOf(old?.type ?: "expense") };
    var account by remember { mutableStateOf(old?.account_id ?: accounts.firstOrNull()?.id ?: 0) };
    var category by remember { mutableStateOf(old?.category_id) };
    var date by remember {
        mutableStateOf(
            old?.transaction_date ?: java.time.LocalDate.now().toString()
        )
    }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(if (old == null) "Nova transação" else "Editar transação") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    desc,
                    { desc = it },
                    label = { Text("Descrição") }); OutlinedTextField(
                amount,
                { amount = it },
                label = { Text("Valor") }); OutlinedTextField(
                date,
                { date = it },
                label = { Text("Data (AAAA-MM-DD)") })
                Row {
                    FilterChip(
                        type == "income",
                        { type = "income" },
                        label = { Text("Receita") }); Spacer(Modifier.width(8.dp)); FilterChip(
                    type == "expense",
                    { type = "expense" },
                    label = { Text("Despesa") })
                }
                Text(
                    "Conta",
                    style = MaterialTheme.typography.labelMedium
                ); accounts.forEach {
                FilterChip(
                    account == it.id,
                    { account = it.id },
                    label = { Text(it.name) })
            }
                Text(
                    "Categoria",
                    style = MaterialTheme.typography.labelMedium
                ); categories.filter { it.type == type }.forEach {
                FilterChip(
                    category == it.id,
                    { category = it.id },
                    label = { Text(it.name) })
            }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = amount.replace(',', '.')
                    .toDoubleOrNull(); if (desc.isNotBlank() && value != null && value > 0 && account > 0) save(
                old?.id ?: 0,
                desc,
                value,
                type,
                account,
                category,
                date
            )
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancelar") } })
}

@Composable
private fun AccountsScreen(
    vm: FinanceViewModel,
    accounts: List<AccountDto>,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) };
    var editing by remember { mutableStateOf<AccountDto?>(null) };
    var name by remember { mutableStateOf("") };
    var balance by remember { mutableStateOf("0") }
    Column(modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Contas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            ); Button(onClick = {
            editing = null; name = ""; balance = "0"; show = true
        }) { Text("Adicionar") }
        }
        LazyColumn {
            items(accounts, key = { it.id }) { a ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text(a.name) },
                            supportingContent = { Text("Saldo: ${money(a.balance)}") }); Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                editing = a; name = a.name; balance = a.balance.toString(); show =
                                true
                            }) { Text("Editar") }; TextButton(onClick = { vm.deleteAccount(a.id) }) {
                        Text(
                            "Excluir"
                        )
                    }
                    }
                    }
                }
            }
        }
    }
    if (show) AlertDialog(
        onDismissRequest = { show = false },
        title = { Text(if (editing == null) "Nova conta" else "Editar conta") },
        text = {
            Column {
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text("Nome da conta") }); OutlinedTextField(
                balance,
                { balance = it },
                label = { Text("Saldo inicial") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val v = balance.replace(',', '.')
                    .toDoubleOrNull(); if (name.isNotBlank() && v != null) {
                vm.saveAccount(editing?.id ?: 0, name, v); show = false
            }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = { show = false }) { Text("Cancelar") } })
}

@Composable
private fun CategoriesScreen(
    vm: FinanceViewModel,
    categories: List<CategoryDto>,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) };
    var editing by remember { mutableStateOf<CategoryDto?>(null) };
    var name by remember { mutableStateOf("") };
    var type by remember { mutableStateOf("expense") }
    Column(modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Categorias",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            ); Button(onClick = {
            editing = null; name = ""; type = "expense"; show = true
        }) { Text("Adicionar") }
        }
        LazyColumn {
            items(categories, key = { it.id }) { c ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text(c.name) },
                            supportingContent = { Text(if (c.type == "income") "Receita" else "Despesa") }); Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                editing = c; name = c.name; type = c.type; show = true
                            }) { Text("Editar") }; TextButton(onClick = { vm.deleteCategory(c.id) }) {
                        Text(
                            "Excluir"
                        )
                    }
                    }
                    }
                }
            }
        }
    }
    if (show) AlertDialog(
        onDismissRequest = { show = false },
        title = { Text(if (editing == null) "Nova categoria" else "Editar categoria") },
        text = {
            Column {
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text("Nome") }); Row {
                FilterChip(
                    type == "income",
                    { type = "income" },
                    label = { Text("Receita") }); Spacer(Modifier.width(8.dp)); FilterChip(
                type == "expense",
                { type = "expense" },
                label = { Text("Despesa") })
            }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    vm.saveCategory(editing?.id ?: 0, name, type); show = false
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = { show = false }) { Text("Cancelar") } })
}

@Composable
private fun SettingsScreen(
    vm: FinanceViewModel,
    name: String,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val dark by vm.dark.collectAsState(); Column(
        modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            "Configurações",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        ); Spacer(Modifier.height(20.dp)); Text("Usuário: $name"); HorizontalDivider(); ListItem(
        headlineContent = { Text("Tema escuro") },
        supportingContent = { Text("Preferência salva no DataStore") },
        trailingContent = {
            Switch(dark,
                { vm.toggleDark(it) })
        }); Spacer(Modifier.height(20.dp)); OutlinedButton(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth()
    ) { Text("Sair da conta") }
    }
}
