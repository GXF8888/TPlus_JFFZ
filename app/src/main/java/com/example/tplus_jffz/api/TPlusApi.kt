package com.example.tplus_jffz.api

import com.example.tplus_jffz.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface TPlusApi {

    // Login
    @POST("/TPlus_JFFZ/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("/TPlus_JFFZ/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("/TPlus_JFFZ/unlogin")
    suspend fun checkLogin(): Response<ApiResponse<Unit>>

    // Account
    @GET("/TPlus_JFFZ/getaccount")
    suspend fun getAccounts(): Response<AccountListResponse>

    // Link (Server config)
    @POST("/TPlus_JFFZ/link")
    suspend fun linkServer(@Body request: LinkRequest): Response<LinkResponse>

    // Update Password
    @POST("/TPlus_JFFZ/updateuserpwd")
    suspend fun updatePassword(@Body request: UpdatePwdRequest): Response<UpdatePwdResponse>

    // Sale Delivery
    @GET("/TPlus_JFFZ/sa_getsaleorder")
    suspend fun getSaleOrders(): Response<ApiResponse<List<SaleOrder>>>

    @GET("/TPlus_JFFZ/sa_getsaleorderinfo")
    suspend fun getSaleOrderInfo(@Query("id") id: String): Response<ApiResponse<SaleOrder>>

    @POST("/TPlus_JFFZ/sa_addsaleorder")
    suspend fun addSaleOrder(@Body request: SaleOrderRequest): Response<ApiResponse<SaleOrder>>

    @POST("/TPlus_JFFZ/sa_delsaleorder")
    suspend fun deleteSaleOrder(@Body map: Map<String, String>): Response<ApiResponse<Unit>>

    @POST("/TPlus_JFFZ/sa_udesaleorder")
    suspend fun updateSaleOrder(@Body request: SaleOrder): Response<ApiResponse<SaleOrder>>

    // RD Record (Inventory In)
    @GET("/TPlus_JFFZ/st_getrdrecord")
    suspend fun getRDRecords(): Response<ApiResponse<List<RDRecord>>>

    @GET("/TPlus_JFFZ/st_getrdrecordinfo")
    suspend fun getRDRecordInfo(@Query("id") id: String): Response<ApiResponse<RDRecord>>

    @POST("/TPlus_JFFZ/st_addrdrecord")
    suspend fun addRDRecord(@Body request: RDRecordRequest): Response<ApiResponse<RDRecord>>

    @POST("/TPlus_JFFZ/st_delrdrecord")
    suspend fun deleteRDRecord(@Body map: Map<String, String>): Response<ApiResponse<Unit>>

    @POST("/TPlus_JFFZ/st_uderdrecord")
    suspend fun updateRDRecord(@Body request: RDRecord): Response<ApiResponse<RDRecord>>

    // Material query by barcode
    @GET("/TPlus_JFFZ/getmaterialbycode")
    suspend fun getMaterialByBarcode(@Query("barcode") barcode: String): Response<ApiResponse<Material>>

    // Current Stock
    @GET("/TPlus_JFFZ/st_currentstock")
    suspend fun getCurrentStock(@Query("materialCode") materialCode: String? = null): Response<ApiResponse<List<StockQuery>>>

    // Trans Voucher (Transfer)
    @GET("/TPlus_JFFZ/db_gettransvoucherinfo")
    suspend fun getTransVoucherInfo(@Query("id") id: String): Response<ApiResponse<TransVoucher>>

    @POST("/TPlus_JFFZ/db_addtransvoucher")
    suspend fun addTransVoucher(@Body request: TransVoucherRequest): Response<ApiResponse<TransVoucher>>

    @POST("/TPlus_JFFZ/db_deltransvoucher")
    suspend fun deleteTransVoucher(@Body map: Map<String, String>): Response<ApiResponse<Unit>>

    // Upload
    @Multipart
    @POST("/TPlus_JFFZ/upload")
    suspend fun uploadFile(@Part file: okhttp3.MultipartBody.Part): Response<ApiResponse<String>>
}
