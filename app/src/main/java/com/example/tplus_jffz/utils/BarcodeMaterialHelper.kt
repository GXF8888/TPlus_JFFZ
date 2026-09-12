package com.example.tplus_jffz.utils

import android.content.Context
import com.example.tplus_jffz.api.RetrofitClient
import com.example.tplus_jffz.data.db.AppDatabase
import com.example.tplus_jffz.data.db.MaterialCacheEntity
import com.example.tplus_jffz.data.model.Material
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object BarcodeMaterialHelper {

    /** Cache validity duration: 7 days */
    private val CACHE_VALIDITY_MS = TimeUnit.DAYS.toMillis(7)

    /**
     * Query material by barcode with offline-first strategy:
     * 1. Return cache immediately if not expired
     * 2. If expired or not cached, try network
     * 3. If network fails, return expired cache as fallback
     * 4. If nothing found, return failure
     */
    suspend fun queryMaterialByBarcode(
        context: Context,
        barcode: String
    ): Result<Material> = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.materialDao()
        val now = System.currentTimeMillis()

        // Step 1: Check local cache
        val cached = dao.getByBarcode(barcode)
        if (cached != null && (now - cached.lastUpdated) < CACHE_VALIDITY_MS) {
            return@withContext Result.success(cached.toMaterial())
        }

        // Step 2: Try network
        try {
            val api = RetrofitClient.getApi(context)
            val response = api.getMaterialByBarcode(barcode)
            if (response.isSuccessful) {
                val material = response.body()?.data
                if (material != null) {
                    // Write back to cache
                    dao.insert(MaterialCacheEntity.fromMaterial(barcode, material))
                    return@withContext Result.success(material)
                }
            }
        } catch (e: Exception) {
            // Network error — fall through to use stale cache or fail
        }

        // Step 3: Fallback to stale cache
        if (cached != null) {
            return@withContext Result.success(cached.toMaterial())
        }

        // Step 4: Not found
        Result.failure(Exception("未找到条码对应的物料: $barcode"))
    }

    /**
     * Batch download all barcodes from server and cache locally.
     * Call this when device has good network (e.g., via IndexActivity sync button).
     */
    suspend fun syncAllMaterials(context: Context): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // NOTE: If your T+ server has an endpoint to list all barcodes,
            // replace this with that API call.
            // For now, we clear expired entries and return count.
            val db = AppDatabase.getDatabase(context)
            val dao = db.materialDao()
            val now = System.currentTimeMillis()
            dao.deleteExpired(now - CACHE_VALIDITY_MS)
            Result.success(dao.count())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Clear all cached materials */
    suspend fun clearCache(context: Context) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).materialDao().clearAll()
    }
}
