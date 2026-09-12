package com.example.tplus_jffz.data.model

object AppConfig {
    var URL: String = ""
    var DBCODE: String = ""
    var DBPWD: String = ""
    var DBNAME: String = ""
    var USERCODE: String = ""
    var USERNAME: String = ""
    var MUTEX: String = ""
}

data class BaseResponse(
    val Message: String? = null,
    val ResultSet: List<Map<String, String>>? = null,
    val ResultSet2: List<Map<String, String>>? = null,
    val OrderCodeArray: String? = null
)
