package com.example.tplus_jffz.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var retrofit: Retrofit? = null
    private const val PREFS_NAME = "link_prefs"
    private const val KEY_URL = "url"
    private const val KEY_DBNAME = "dbname"
    private const val KEY_DBUSER = "dbuser"
    private const val KEY_DBPWD = "dbpwd"

    fun getApi(context: Context): TPlusApi {
        val baseUrl = getBaseUrl(context)
        if (retrofit == null || retrofit?.baseUrl()?.toString() != baseUrl) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(TPlusApi::class.java)
    }

    fun getBaseUrl(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_URL, "http://192.168.1.202:8083") ?: "http://192.168.1.202:8083"
    }

    fun getDatabaseName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DBNAME, "UFTData325084_000008") ?: "UFTData325084_000008"
    }

    fun getDatabaseUser(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DBUSER, "tplusdbadmin") ?: "tplusdbadmin"
    }

    fun getDatabasePassword(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DBPWD, "tplus_12345") ?: "tplus_12345"
    }

    fun saveConfig(context: Context, url: String, dbName: String, dbUser: String, dbPwd: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_URL, url)
            putString(KEY_DBNAME, dbName)
            putString(KEY_DBUSER, dbUser)
            putString(KEY_DBPWD, dbPwd)
            apply()
        }
    }
}
