package com.example.financeapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// API hospedada no alwaysdata.
const val DEFAULT_BASE_URL = "https://financeapp.alwaysdata.net/"

data class UserDto(val id: Int, val name: String, val username: String)
data class AuthResponse(val success: Boolean, val message: String, val user: UserDto? = null)
data class ApiMessage(val success: Boolean, val message: String)
data class TransactionDto(
    val id: Int, val account_id: Int, val category_id: Int?, val description: String,
    val amount: Double, val type: String, val transaction_date: String
)
data class AccountDto(val id: Int, val name: String, val balance: Double, val user_id: Int)
data class CategoryDto(val id: Int, val name: String, val type: String, val user_id: Int)
data class TransactionListResponse(val success: Boolean, val data: List<TransactionDto> = emptyList(), val message: String = "")
data class AccountListResponse(val success: Boolean, val data: List<AccountDto> = emptyList(), val message: String = "")
data class CategoryListResponse(val success: Boolean, val data: List<CategoryDto> = emptyList(), val message: String = "")

interface FinanceApi {
    @POST("api.php?action=login") suspend fun login(@Body body: Map<String, String>): AuthResponse
    @POST("api.php?action=register") suspend fun register(@Body body: Map<String, String>): ApiMessage
    @GET("api.php?action=transactions") suspend fun transactions(@Query("user_id") userId: Int): TransactionListResponse
    @GET("api.php?action=accounts") suspend fun accounts(@Query("user_id") userId: Int): AccountListResponse
    @GET("api.php?action=categories") suspend fun categories(@Query("user_id") userId: Int): CategoryListResponse
    @POST("api.php?action=save_transaction") suspend fun saveTransaction(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiMessage
    @POST("api.php?action=delete_transaction") suspend fun deleteTransaction(@Body body: Map<String, Int>): ApiMessage
    @POST("api.php?action=save_account") suspend fun saveAccount(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiMessage
    @POST("api.php?action=delete_account") suspend fun deleteAccount(@Body body: Map<String, Int>): ApiMessage
    @POST("api.php?action=save_category") suspend fun saveCategory(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiMessage
    @POST("api.php?action=delete_category") suspend fun deleteCategory(@Body body: Map<String, Int>): ApiMessage
}

object ApiClient {
    val service: FinanceApi = build(DEFAULT_BASE_URL)

    private fun build(url: String): FinanceApi =
        Retrofit.Builder().baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(FinanceApi::class.java)
}
