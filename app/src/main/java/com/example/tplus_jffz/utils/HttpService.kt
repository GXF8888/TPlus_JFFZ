package com.example.tplus_jffz.utils

import android.content.Context
import com.example.tplus_jffz.api.RetrofitClient
import com.example.tplus_jffz.data.model.AppConfig
import com.example.tplus_jffz.data.model.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

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
                    val raw = resp.body()?.string() ?: "{}"
                    parseResponse(raw)
                } else {
                    BaseResponse(Message = "服务器返回错误: ${resp.code()}", rawJson = "")
                }
            } catch (e: Exception) {
                BaseResponse(Message = "异常：${e.message}", rawJson = "")
            }
        }
    }

    private fun parseResponse(raw: String): BaseResponse {
        val jo = try { JSONObject(raw) } catch (e: Exception) { return BaseResponse(Message = "解析失败: ${e.message}", rawJson = raw) }
        val message = if (jo.has("Message")) jo.optString("Message") else if (jo.has("message")) jo.optString("message") else ""
        val orderCodeArray = if (jo.has("OrderCodeArray")) jo.optString("OrderCodeArray") else ""

        // parse ResultSet (could be JSONObject for inventory, JSONArray for order)
        val resultSet = mutableListOf<Map<String, String>>()
        if (jo.has("ResultSet")) {
            val rs = jo.get("ResultSet")
            if (rs is JSONObject) {
                resultSet.add(jsonObjectToMap(rs))
            } else if (rs is JSONArray) {
                for (i in 0 until rs.length()) {
                    val obj = rs.getJSONObject(i)
                    resultSet.add(jsonObjectToMap(obj))
                }
            }
        }

        // parse ResultSet2
        val resultSet2 = mutableListOf<Map<String, String>>()
        if (jo.has("ResultSet2")) {
            val rs = jo.getJSONArray("ResultSet2")
            for (i in 0 until rs.length()) {
                val obj = rs.getJSONObject(i)
                resultSet2.add(jsonObjectToMap(obj))
            }
        }

        return BaseResponse(
            Message = message,
            ResultSet = resultSet,
            ResultSet2 = resultSet2,
            OrderCodeArray = orderCodeArray,
            rawJson = raw
        )
    }

    private fun jsonObjectToMap(jo: JSONObject): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val keys = jo.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = if (jo.isNull(key)) "" else jo.get(key).toString()
        }
        return map
    }
}
