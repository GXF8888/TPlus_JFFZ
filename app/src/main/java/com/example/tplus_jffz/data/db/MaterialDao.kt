package com.example.tplus_jffz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MaterialDao {

    @Query("SELECT * FROM material_cache WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): MaterialCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MaterialCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MaterialCacheEntity>)

    @Update
    suspend fun update(entity: MaterialCacheEntity)

    @Query("DELETE FROM material_cache WHERE barcode = :barcode")
    suspend fun deleteByBarcode(barcode: String)

    @Query("SELECT COUNT(*) FROM material_cache")
    suspend fun count(): Int

    @Query("DELETE FROM material_cache")
    suspend fun clearAll()

    @Query("DELETE FROM material_cache WHERE lastUpdated < :expireTime")
    suspend fun deleteExpired(expireTime: Long)
}
