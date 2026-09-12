package com.example.tplus_jffz.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface TPlusApi {
    @POST
    suspend fun post(@Url url: String, @Body body: Map<String, String>): Response<ResponseBody>
}
