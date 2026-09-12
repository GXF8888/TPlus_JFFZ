package com.example.tplus_jffz.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tplus_jffz.data.model.Material

@Entity(
    tableName = "material_cache",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class MaterialCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val code: String,
    val name: String,
    val specification: String? = null,
    val unit: String? = null,
    val price: Double = 0.0,
    val stockQty: Double = 0.0,
    val warehouseId: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toMaterial(): Material = Material(
        code = code,
        name = name,
        specification = specification,
        unit = unit,
        price = price,
        stockQty = stockQty,
        warehouseId = warehouseId
    )

    companion object {
        fun fromMaterial(barcode: String, material: Material): MaterialCacheEntity =
            MaterialCacheEntity(
                barcode = barcode,
                code = material.code,
                name = material.name,
                specification = material.specification,
                unit = material.unit,
                price = material.price,
                stockQty = material.stockQty,
                warehouseId = material.warehouseId,
                lastUpdated = System.currentTimeMillis()
            )
    }
}
