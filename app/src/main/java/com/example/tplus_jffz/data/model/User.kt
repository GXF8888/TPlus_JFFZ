package com.example.tplus_jffz.data.model

data class User(
    val userId: String,
    val userName: String,
    val accountId: String? = null,
    val accountName: String? = null,
    val token: String? = null
)

data class LoginRequest(
    val userCode: String,
    val password: String,
    val accountId: String? = null
)

data class LoginResponse(
    val success: Boolean = false,
    val message: String? = null,
    val user: User? = null
)

data class Account(
    val id: String,
    val name: String
)
