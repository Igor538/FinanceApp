package com.example.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()
    private val _transactions = MutableStateFlow<List<TransactionDto>>(emptyList())
    val transactions: StateFlow<List<TransactionDto>> = _transactions.asStateFlow()
    private val _accounts = MutableStateFlow<List<AccountDto>>(emptyList())
    val accounts: StateFlow<List<AccountDto>> = _accounts.asStateFlow()
    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()
    private val _dark = MutableStateFlow(false)
    val dark: StateFlow<Boolean> = _dark.asStateFlow()
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    init {
        viewModelScope.launch { _dark.value = prefs.darkTheme.first() }
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        if (username.isBlank() || password.isBlank()) {
            _message.value = "Informe usuário e senha."
            return@launch
        }
        _busy.value = true
        try {
            val r = repository.login(username, password)
            if (r.success && r.user != null) {
                _user.value = r.user
                prefs.setUserId(r.user.id)
                loadAll(r.user.id)
                _message.value = "Login realizado."
            } else {
                _message.value = r.message
            }
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
                "Preencha os campos. A senha deve ter ao menos 6 caracteres."
            return@launch
        }
        try {
            val r = repository.register(name, username, password)
            _message.value = r.message
        } catch (_: Exception) {
            _message.value = "Falha ao cadastrar. Verifique a API."
        }
    }

    private suspend fun loadAll(id: Int) {
        try {
            _transactions.value = repository.transactions(id).data
            _accounts.value = repository.accounts(id).data
            _categories.value = repository.categories(id).data
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
            if (id > 0) body["id"] = id
            if (categoryId != null) body["category_id"] = categoryId
            _message.value = repository.saveTransaction(body).message
            loadAll(uid)
        } catch (_: Exception) {
            _message.value = "Erro ao salvar transação."
        }
    }

    fun deleteTransaction(id: Int) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first()
        try {
            _message.value = repository.deleteTransaction(id, uid).message
            loadAll(uid)
        } catch (_: Exception) {
            _message.value = "Erro ao excluir transação."
        }
    }

    fun saveAccount(id: Int, name: String, balance: Double) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first()
        try {
            val b = mutableMapOf<String, Any>(
                "user_id" to uid,
                "name" to name,
                "balance" to balance
            )
            if (id > 0) b["id"] = id
            _message.value = repository.saveAccount(b).message
            loadAll(uid)
        } catch (_: Exception) {
            _message.value = "Erro ao salvar conta."
        }
    }

    fun deleteAccount(id: Int) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first()
        try {
            _message.value = repository.deleteAccount(id, uid).message
            loadAll(uid)
        } catch (_: Exception) {
            _message.value = "Erro ao excluir conta."
        }
    }

    fun saveCategory(id: Int, name: String, type: String) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first()
        try {
            val b = mutableMapOf<String, Any>(
                "user_id" to uid,
                "name" to name,
                "type" to type
            )
            if (id > 0) b["id"] = id
            _message.value = repository.saveCategory(b).message
            loadAll(uid)
        } catch (_: Exception) {
            _message.value = "Erro ao salvar categoria."
        }
    }

    fun deleteCategory(id: Int) = viewModelScope.launch {
        val uid = _user.value?.id ?: prefs.userId.first()
        try {
            _message.value = repository.deleteCategory(id, uid).message
            loadAll(uid)
        } catch (_: Exception) {
            _message.value = "Erro ao excluir categoria."
        }
    }

    fun toggleDark(value: Boolean) =
        viewModelScope.launch { _dark.value = value; prefs.setDarkTheme(value) }

    fun setServerUrl(value: String) =
        viewModelScope.launch {
            prefs.setServerUrl(value)
            ApiClient.setBaseUrl(value)
        }

    fun logout() = viewModelScope.launch {
        prefs.clearUser()
        _user.value = null
        _transactions.value = emptyList()
        _accounts.value = emptyList()
        _categories.value = emptyList()
    }

    fun consumeMessage() {
        _message.value = ""
    }
}

// ============================================================================
// TEMA — paleta própria (índigo no claro, azul-profundidade no escuro)
// ============================================================================

private val DarkGreen = Color(0xFF4ADE80)
private val DarkRed = Color(0xFFF87171)

@Composable
private fun financeTheme(dark: Boolean, content: @Composable () -> Unit) {
    val scheme = if (dark) {
        darkColorScheme(
            primary = Color(0xFF818CF8),
            onPrimary = Color(0xFF1E1B4B),
            primaryContainer = Color(0xFF3730A3),
            onPrimaryContainer = Color(0xFFE0E7FF),
            secondary = Color(0xFFA78BFA),
            onSecondary = Color(0xFF1E1B4B),
            background = Color(0xFF16151D),
            surface = Color(0xFF1F1E28),
            surfaceVariant = Color(0xFF2A2934),
            onSurface = Color(0xFFF4F4F6),
            onSurfaceVariant = Color(0xFFB7B6C2)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF5B5BD6),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE5E4FB),
            onPrimaryContainer = Color(0xFF272788),
            secondary = Color(0xFF5B5BD6),
            onSecondary = Color.White,
            background = Color(0xFFF6F6F8),
            surface = Color.White,
            surfaceVariant = Color(0xFFEEEFF4),
            onSurface = Color(0xFF1B1B20),
            onSurfaceVariant = Color(0xFF63636E)
        )
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

private val CardShape = RoundedCornerShape(18.dp)
private val BigCardShape = RoundedCornerShape(24.dp)
private val BtnShape = RoundedCornerShape(14.dp)

@Composable
private fun green(): Color = if (MaterialTheme.colorScheme.background == Color(0xFF16151D)) DarkGreen else Color(0xFF16A34A)

@Composable
private fun red(): Color = if (MaterialTheme.colorScheme.background == Color(0xFF16151D)) DarkRed else Color(0xFFDC2626)

// ============================================================================
// NAVEGAÇÃO RAIZ
// ============================================================================

@Composable
fun FinanceRoot() {
    val context = LocalContext.current
    val vm: FinanceViewModel =
        viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FinanceViewModel(FinanceRepository(), Preferences(context)) as T
        })
    val dark by vm.dark.collectAsState()
    financeTheme(dark) {
        val nav = rememberNavController()
        NavHost(navController = nav, startDestination = "login") {
            composable("login") { LoginScreen(vm, nav) }
            composable("home") { MainShell(vm, nav) }
        }
    }
}

// ============================================================================
// LOGIN
// ============================================================================

@Composable
private fun LoginScreen(vm: FinanceViewModel, nav: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var registering by remember { mutableStateOf(false) }
    val busy by vm.busy.collectAsState()
    val message by vm.message.collectAsState()
    val user by vm.user.collectAsState()
    LaunchedEffect(user) {
        if (user != null) nav.navigate("home") { popUpTo("login") { inclusive = true } }
    }
    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            kotlinx.coroutines.delay(3500); vm.consumeMessage()
        }
    }
    val cs = MaterialTheme.colorScheme

    Surface(Modifier.fillMaxSize(), color = cs.background) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp)
            ) {
            Text(
                "FinanceApp",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Organize sua vida financeira",
                color = cs.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            if (registering) {
                OutlinedTextField(
                    name, { name = it },
                    label = { Text("Nome completo") },
                    singleLine = true,
                    shape = BtnShape,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }
            OutlinedTextField(
                username, { username = it },
                label = { Text("Usuário") },
                singleLine = true,
                shape = BtnShape,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            var showPassword by remember { mutableStateOf(false) }
            OutlinedTextField(
                password, { password = it },
                label = { Text("Senha") },
                singleLine = true,
                shape = BtnShape,
                visualTransformation = if (showPassword) {
                    androidx.compose.ui.text.input.VisualTransformation.None
                } else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(
                        onClick = { showPassword = !showPassword },
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text(
                            if (showPassword) "🙈" else "👁",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (registering) vm.register(name, username, password)
                    else vm.login(username, password)
                },
                enabled = !busy,
                shape = BtnShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    when {
                        busy -> "Aguarde..."
                        registering -> "Criar conta"
                        else -> "Entrar"
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(
                onClick = { registering = !registering },
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(
                    if (registering) "Já tenho conta · Entrar"
                    else "Não tem conta? Cadastre-se"
                )
            }

            if (!registering) {
                Spacer(Modifier.height(18.dp))
                Card(
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Conta de demonstração",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Usuário:",
                                color = cs.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "admin",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(
                                "Senha:",
                                color = cs.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "123456",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(
                            onClick = { username = "admin"; password = "123456" }
                        ) {
                            Text("Preencher dados de teste")
                        }
                    }
                }
            }

            if (message.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    message,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            }
        }
    }
}

// ============================================================================
// CASCA PRINCIPAL (bottom bar)
// ============================================================================

@Composable
private fun MainShell(vm: FinanceViewModel, nav: NavHostController) {
    var tab by remember { mutableStateOf("Início") }
    val user by vm.user.collectAsState()
    val transactions by vm.transactions.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val categories by vm.categories.collectAsState()
    val message by vm.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val cs = MaterialTheme.colorScheme
    LaunchedEffect(Unit) { vm.reload() }
    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            snackbarHostState.showSnackbar(message); vm.consumeMessage()
        }
    }
    Scaffold(
        containerColor = cs.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = cs.surface, tonalElevation = 2.dp) {
                listOf(
                    "Início", "Transações", "Contas", "Categorias", "Ajustes"
                ).forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Text(iconFor(item), style = MaterialTheme.typography.titleMedium) },
                        label = { Text(item, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }) { padding ->
        Surface(Modifier.padding(padding), color = cs.background) {
            when (tab) {
                "Início" -> DashboardScreen(
                    user?.name ?: "Usuário", transactions, accounts
                )

                "Transações" -> TransactionsScreen(
                    vm, transactions, accounts, categories
                )

                "Contas" -> AccountsScreen(vm, accounts)
                "Categorias" -> CategoriesScreen(vm, categories)
                else -> SettingsScreen(
                    vm, user?.name ?: "Usuário",
                    onLogout = {
                        vm.logout(); nav.navigate("login") { popUpTo(0) }
                    })
            }
        }
    }
}

private fun iconFor(item: String) = when (item) {
    "Início" -> "⌂"; "Transações" -> "⇅"; "Contas" -> "▣"; "Categorias" -> "≡"; else -> "⚙"
}

private fun money(v: Double) =
    NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(v)

// ============================================================================
// DASHBOARD
// ============================================================================

@Composable
private fun DashboardScreen(
    name: String,
    tx: List<TransactionDto>,
    accounts: List<AccountDto>,
    modifier: Modifier = Modifier
) {
    val income = tx.filter { it.type == "income" }.sumOf { it.amount }
    val expense = tx.filter { it.type == "expense" }.sumOf { it.amount }
    val cs = MaterialTheme.colorScheme

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Olá, $name",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Aqui está seu resumo financeiro",
            color = cs.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(20.dp))

        // Cartão principal: saldo
        Card(
            shape = BigCardShape,
            colors = CardDefaults.cardColors(containerColor = cs.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Saldo consolidado",
                    color = cs.onPrimary.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    money(accounts.sumOf { it.balance }),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = cs.onPrimary
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RateCard(
                "Receitas", income, green(), false, Modifier.weight(1f)
            )
            RateCard(
                "Despesas", expense, red(), true, Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Movimentações recentes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        if (tx.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏦", fontSize = MaterialTheme.typography.displaySmall.fontSize)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Sem movimentações por aqui",
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            tx.take(5).forEach {
                RecentTxRow(it)
            }
        }

        Spacer(Modifier.height(8.dp))
        Column {
            Text(
                "Seus cartões",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            accounts.take(2).forEach { account ->
                AccountSmallCard(account)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RateCard(
    label: String,
    value: Double,
    color: Color,
    isExpense: Boolean,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    Card(shape = CardShape, modifier = modifier) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isExpense) "↓" else "↑",
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                money(value),
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun RecentTxRow(t: TransactionDto) {
    val cs = MaterialTheme.colorScheme
    ListItem(
        headlineContent = {
            Text(t.description, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text(
                "${t.transaction_date}  ·  ${if (t.type == "income") "Receita" else "Despesa"}",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant
            )
        },
        trailingContent = {
            Text(
                (if (t.type == "income") "+" else "−") + money(t.amount),
                fontWeight = FontWeight.Bold,
                color = if (t.type == "income") green() else red()
            )
        }
    )
}

@Composable
private fun AccountSmallCard(a: AccountDto) {
    val cs = MaterialTheme.colorScheme
    Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = cs.surfaceVariant)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(38.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("▣", color = cs.primary, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(a.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "Saldo disponível",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
            Text(money(a.balance), fontWeight = FontWeight.Bold)
        }
    }
}

// ============================================================================
// TRANSAÇÕES
// ============================================================================

@Composable
private fun TransactionsScreen(
    vm: FinanceViewModel,
    tx: List<TransactionDto>,
    accounts: List<AccountDto>,
    categories: List<CategoryDto>,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TransactionDto?>(null) }
    val cs = MaterialTheme.colorScheme

    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Transações",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { editing = null; show = true },
                shape = BtnShape,
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) { Text("+ Nova") }
        }
        Spacer(Modifier.height(16.dp))
        if (tx.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧾", fontSize = MaterialTheme.typography.displaySmall.fontSize)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Cadastre sua primeira movimentação",
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn {
                items(tx, key = { it.id }) { t ->
                    TxListItem(
                        t = t,
                        onEdit = { editing = t; show = true },
                        onDelete = { vm.deleteTransaction(t.id) }
                    )
                }
            }
        }
    }
    if (show) TransactionDialog(
        editing, accounts, categories,
        { d, a, v, typ, acc, cat, date ->
            vm.saveTransaction(d, a, v, typ, acc, cat, date); show = false
        },
        { show = false })
}

@Composable
private fun TxListItem(
    t: TransactionDto,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Column {
            RecentTxRow(t)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onDelete) {
                    Text("Excluir", color = cs.error)
                }
            }
        }
    }
}

@Composable
private fun TransactionDialog(
    old: TransactionDto?,
    accounts: List<AccountDto>,
    categories: List<CategoryDto>,
    save: (Int, String, Double, String, Int, Int?, String) -> Unit,
    dismiss: () -> Unit
) {
    var desc by remember { mutableStateOf(old?.description ?: "") }
    var amount by remember { mutableStateOf(old?.amount?.toString() ?: "") }
    var type by remember { mutableStateOf(old?.type ?: "expense") }
    var account by remember {
        mutableStateOf(old?.account_id ?: accounts.firstOrNull()?.id ?: 0)
    }
    var category by remember { mutableStateOf(old?.category_id) }
    var date by remember {
        mutableStateOf(old?.transaction_date ?: java.time.LocalDate.now().toString())
    }
    AlertDialog(
        onDismissRequest = dismiss,
        title = {
            Text(if (old == null) "Nova transação" else "Editar transação")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    desc, { desc = it },
                    label = { Text("Descrição") },
                    singleLine = true,
                    shape = BtnShape,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    amount, { amount = it },
                    label = { Text("Valor") },
                    singleLine = true,
                    shape = BtnShape,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    date, { date = it },
                    label = { Text("Data (AAAA-MM-DD)") },
                    singleLine = true,
                    shape = BtnShape,
                    modifier = Modifier.fillMaxWidth()
                )
                Row {
                    RowSegment("Receita", type == "income") { type = "income" }
                    Spacer(Modifier.width(8.dp))
                    RowSegment("Despesa", type == "expense") { type = "expense" }
                }
                Text("Conta", style = MaterialTheme.typography.labelMedium)
                accounts.forEach {
                    RowSegment(it.name, account == it.id) { account = it.id }
                }
                Text("Categoria", style = MaterialTheme.typography.labelMedium)
                if (categories.filter { it.type == type }.isEmpty()) {
                    Text(
                        "Nenhuma categoria",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                categories.filter { it.type == type }.forEach {
                    RowSegment(it.name, category == it.id) { category = it.id }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = amount.replace(',', '.')
                    .toDoubleOrNull()
                if (desc.isNotBlank() && value != null && value > 0 && account > 0) save(
                    old?.id ?: 0, desc, value, type, account, category, date
                )
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancelar") } })
}

@Composable
private fun RowSegment(label: String, selected: Boolean, onTap: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onTap,
        label = { Text(label) },
        shape = BtnShape
    )
}

// ============================================================================
// CONTAS
// ============================================================================

@Composable
private fun AccountsScreen(
    vm: FinanceViewModel,
    accounts: List<AccountDto>,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AccountDto?>(null) }
    var name by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("0") }
    val cs = MaterialTheme.colorScheme

    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Contas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    editing = null; name = ""; balance = "0"; show = true
                },
                shape = BtnShape,
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) { Text("+ Nova") }
        }
        Spacer(Modifier.height(16.dp))

        if (accounts.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("💳", fontSize = MaterialTheme.typography.displaySmall.fontSize)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Nenhuma conta ainda",
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Resumo no topo
            Card(
                shape = BigCardShape,
                colors = CardDefaults.cardColors(containerColor = cs.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Saldo em todas as contas",
                        color = cs.onPrimary.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        money(accounts.sumOf { it.balance }),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = cs.onPrimary
                    )
                    Text(
                        "${accounts.size} conta${if (accounts.size > 1) "s" else ""}",
                        color = cs.onPrimary.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            LazyColumn {
                items(accounts, key = { it.id }) { a ->
                    AccountListItem(
                        account = a,
                        onEdit = {
                            editing = a; name = a.name
                            balance = a.balance.toString(); show = true
                        },
                        onDelete = { vm.deleteAccount(a.id) }
                    )
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
                    name, { name = it },
                    label = { Text("Nome da conta") },
                    singleLine = true,
                    shape = BtnShape,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    balance, { balance = it },
                    label = { Text("Saldo inicial") },
                    singleLine = true,
                    shape = BtnShape,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val v = balance.replace(',', '.')
                    .toDoubleOrNull()
                if (name.isNotBlank() && v != null) {
                    vm.saveAccount(editing?.id ?: 0, name, v); show = false
                }
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = { show = false }) { Text("Cancelar") }
        })
}

@Composable
private fun AccountListItem(
    account: AccountDto,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val positive = account.balance >= 0
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 72.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circular com a inicial
            Box(
                Modifier
                    .size(46.dp)
                    .background(cs.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    account.name.take(1).uppercase(),
                    color = cs.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    account.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Saldo disponível",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    money(account.balance),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (positive) green() else cs.error
                )
                Row {
                    TextButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(vertical = 2.dp, horizontal = 8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Editar", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(
                        onClick = onDelete,
                        contentPadding = PaddingValues(vertical = 2.dp, horizontal = 8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Excluir", style = MaterialTheme.typography.labelSmall, color = cs.error)
                    }
                }
            }
        }
    }
}

// ============================================================================
// CATEGORIAS
// ============================================================================

@Composable
private fun CategoriesScreen(
    vm: FinanceViewModel,
    categories: List<CategoryDto>,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CategoryDto?>(null) }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    val cs = MaterialTheme.colorScheme

    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Categorias",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    editing = null; name = ""; type = "expense"; show = true
                },
                shape = BtnShape,
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) { Text("+ Nova") }
        }
        Spacer(Modifier.height(16.dp))
        if (categories.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏷", fontSize = MaterialTheme.typography.displaySmall.fontSize)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Nenhuma categoria ainda",
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn {
                items(categories, key = { it.id }) { c ->
                    Card(
                        shape = CardShape,
                        colors = CardDefaults.cardColors(containerColor = cs.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(c.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (c.type == "income") "Receita" else "Despesa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (c.type == "income") green() else red()
                                )
                            }
                            TextButton(onClick = {
                                editing = c; name = c.name; type = c.type; show = true
                            }) { Text("Editar") }
                            TextButton(onClick = { vm.deleteCategory(c.id) }) {
                                Text("Excluir", color = cs.error)
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
                    name, { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    shape = BtnShape,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    RowSegment("Receita", type == "income") { type = "income" }
                    Spacer(Modifier.width(8.dp))
                    RowSegment("Despesa", type == "expense") { type = "expense" }
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
        dismissButton = {
            TextButton(onClick = { show = false }) { Text("Cancelar") }
        })
}

// ============================================================================
// AJUSTES
// ============================================================================

@Composable
private fun SettingsScreen(
    vm: FinanceViewModel,
    name: String,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val dark by vm.dark.collectAsState()
    val cs = MaterialTheme.colorScheme

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Ajustes",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))

        Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = cs.surface)) {
            Column {
                ListItem(
                    headlineContent = { Text(name, fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        Text("Sua conta", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                )
                HorizontalDivider(color = cs.surfaceVariant)
                ListItem(
                    headlineContent = { Text("Tema escuro") },
                    supportingContent = {
                        Text("Preferência salva no DataStore", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    },
                    trailingContent = {
                        Switch(dark, { vm.toggleDark(it) })
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Servidor da API
        Card(shape = CardShape, colors = CardDefaults.cardColors(containerColor = cs.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Servidor da API",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "No celular, use o endereço do PC na rede Wi-Fi (ipconfig no PC). Ex.: http://192.168.0.15/financeapp_api/",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                var url by remember { mutableStateOf(ApiClient.currentUrl) }
                OutlinedTextField(
                    url, { url = it },
                    label = { Text("Endereço") },
                    singleLine = true,
                    shape = BtnShape,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.setServerUrl(url) },
                    shape = BtnShape,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Salvar servidor") }
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onLogout,
            shape = BtnShape,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Sair da conta", color = cs.error, fontWeight = FontWeight.SemiBold) }
    }
}
