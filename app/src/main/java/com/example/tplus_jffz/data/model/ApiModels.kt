package com.example.tplus_jffz.data.model

data class LinkRequest(
    val serverUrl: String
)

data class LinkResponse(
    val success: Boolean = false,
    val message: String? = null
)

data class AccountListResponse(
    val success: Boolean = false,
    val accounts: List<Account>? = null,
    val message: String? = null
)

data class UpdatePwdRequest(
    val userCode: String,
    val oldPassword: String,
    val newPassword: String
)

data class UpdatePwdResponse(
    val success: Boolean = false,
    val message: String? = null
)

data class Material(
    val code: String,
    val name: String,
    val specification: String? = null,
    val unit: String? = null,
    val price: Double = 0.0,
    val stockQty: Double = 0.0,
    val warehouseId: String? = null
)

data class SaleOrder(
    val id: String? = null,
    val code: String? = null,
    val date: String? = null,
    val customerId: String? = null,
    val customerName: String? = null,
    val warehouseId: String? = null,
    val warehouseName: String? = null,
    val details: List<SaleOrderDetail> = emptyList(),
    val totalQty: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String? = null
)

data class SaleOrderDetail(
    val materialCode: String,
    val materialName: String? = null,
    val specification: String? = null,
    val unit: String? = null,
    val qty: Double = 0.0,
    val price: Double = 0.0,
    val amount: Double = 0.0,
    val batchNo: String? = null
)

data class SaleOrderRequest(
    val customerId: String? = null,
    val warehouseId: String? = null,
    val details: List<SaleOrderDetail> = emptyList()
)

data class RDRecord(
    val id: String? = null,
    val code: String? = null,
    val date: String? = null,
    val supplierId: String? = null,
    val supplierName: String? = null,
    val warehouseId: String? = null,
    val warehouseName: String? = null,
    val details: List<RDRecordDetail> = emptyList(),
    val totalQty: Double = 0.0,
    val status: String? = null
)

data class RDRecordDetail(
    val materialCode: String,
    val materialName: String? = null,
    val specification: String? = null,
    val unit: String? = null,
    val qty: Double = 0.0,
    val batchNo: String? = null,
    val price: Double = 0.0
)

data class RDRecordRequest(
    val supplierId: String? = null,
    val warehouseId: String? = null,
    val details: List<RDRecordDetail> = emptyList()
)

data class TransVoucher(
    val id: String? = null,
    val code: String? = null,
    val date: String? = null,
    val outWarehouseId: String? = null,
    val outWarehouseName: String? = null,
    val inWarehouseId: String? = null,
    val inWarehouseName: String? = null,
    val details: List<TransVoucherDetail> = emptyList(),
    val totalQty: Double = 0.0,
    val status: String? = null
)

data class TransVoucherDetail(
    val materialCode: String,
    val materialName: String? = null,
    val specification: String? = null,
    val unit: String? = null,
    val qty: Double = 0.0,
    val batchNo: String? = null
)

data class TransVoucherRequest(
    val outWarehouseId: String? = null,
    val inWarehouseId: String? = null,
    val details: List<TransVoucherDetail> = emptyList()
)

data class StockQuery(
    val materialCode: String? = null,
    val materialName: String? = null,
    val warehouseId: String? = null,
    val warehouseName: String? = null,
    val qty: Double = 0.0
)

data class ApiResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null,
    val code: Int = 0
)

data class ScanResult(
    val barcode: String,
    val material: Material? = null
)
