package com.example.tplus_jffz.utils

import android.content.Context
import com.example.tplus_jffz.api.RetrofitClient
import com.example.tplus_jffz.data.model.AppConfig
import com.example.tplus_jffz.data.model.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object HttpService {
    fun buildBaseParams(): HashMap<String, String> {
        return hashMapOf(
            "DBCode" to AppConfig.DBCODE,
            "DBPwd" to AppConfig.DBPWD,
            "DBName" to AppConfig.DBNAME,
            "UserCode" to AppConfig.USERCODE,
            "Mutex" to AppConfig.MUTEX
        )
    }

    suspend fun post(context: Context, path: String, params: Map<String, String>): BaseResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val api = RetrofitClient.getApi(context)
                val url = AppConfig.URL + path
                val body = buildBaseParams().apply { putAll(params) }
                val resp = api.post(url, body)
                if (resp.isSuccessful) {
                    resp.body()
                } else {
                    BaseResponse(Message = "服务器返回错误: ${resp.code()}")
                }
            } catch (e: Exception) {
                BaseResponse(Message = "异常：${e.message}")
            }
        }
    }
}
