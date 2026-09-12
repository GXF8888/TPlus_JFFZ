package com.example.tplus_jffz.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val PREFS_NAME = "tplus_prefs"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_DATABASE = "database_name"
    private const val KEY_DB_USER = "db_user"
    private const val KEY_DB_PASSWORD = "db_password"
    private const val DEFAULT_URL = "http://192.168.1.202:8083"

    private var retrofit: Retrofit? = null
    private var api: TPlusApi? = null

    fun getApi(context: Context): TPlusApi {
        if (api == null) {
            api = createRetrofit(context).create(TPlusApi::class.java)
        }
        return api!!
    }

    fun resetApi() {
        api = null
        retrofit = null
    }

    fun getBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BASE_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    fun saveBaseUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BASE_URL, url).apply()
        resetApi()
    }

    fun getDatabaseName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DATABASE, null)
    }

    fun saveDatabaseName(context: Context, database: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DATABASE, database).apply()
    }

    fun getDatabaseUser(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DB_USER, null)
    }

    fun saveDatabaseUser(context: Context, user: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DB_USER, user).apply()
    }

    fun getDatabasePassword(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DB_PASSWORD, null)
    }

    fun saveDatabasePassword(context: Context, password: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DB_PASSWORD, password).apply()
    }

    private fun createRetrofit(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val baseUrl = getBaseUrl(context)
        // Ensure trailing slash
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build().also { retrofit = it }
    }
}
