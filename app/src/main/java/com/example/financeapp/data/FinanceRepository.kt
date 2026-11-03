package com.example.financeapp.data

/** Camada de repositório: centraliza o acesso remoto usado pelo ViewModel. */
class FinanceRepository(private val api: FinanceApi = ApiClient.service) {
    suspend fun login(username: String, password: String) = api.login(mapOf("username" to username, "password" to password))
    suspend fun register(name: String, username: String, password: String) = api.register(mapOf("name" to name, "username" to username, "password" to password))
    suspend fun transactions(userId: Int) = api.transactions(userId)
    suspend fun accounts(userId: Int) = api.accounts(userId)
    suspend fun categories(userId: Int) = api.categories(userId)
    suspend fun saveTransaction(body: Map<String, Any>) = api.saveTransaction(body)
    suspend fun deleteTransaction(id: Int, userId: Int) = api.deleteTransaction(mapOf("id" to id, "user_id" to userId))
    suspend fun saveAccount(body: Map<String, Any>) = api.saveAccount(body)
    suspend fun deleteAccount(id: Int, userId: Int) = api.deleteAccount(mapOf("id" to id, "user_id" to userId))
    suspend fun saveCategory(body: Map<String, Any>) = api.saveCategory(body)
    suspend fun deleteCategory(id: Int, userId: Int) = api.deleteCategory(mapOf("id" to id, "user_id" to userId))
}
