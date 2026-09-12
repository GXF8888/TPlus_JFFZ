package com.example.tplus_jffz.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val userId: String,
    val userName: String,
    val accountId: String? = null,
    val accountName: String? = null,
    val token: String? = null
)

data class LoginRequest(
    @SerializedName("UserCode")
    val userCode: String,

    @SerializedName("UserPwd")
    val password: String,

    @SerializedName("DBCode")
    val DBCode: String? = null,

    @SerializedName("DBName")
    val DBName: String? = null,

    @SerializedName("DBUser")
    val DBUser: String? = null,

    @SerializedName("DBPwd")
    val DBPwd: String? = null,

    @SerializedName("MUTEX")
    val MUTEX: String? = null
)

data class LoginResponse(
    val success: Boolean = false,
    val message: String? = null,
    @SerializedName("Message")
    val serverMessage: String? = null,
    val user: User? = null
)

data class Account(
    val id: String,
    val name: String
)