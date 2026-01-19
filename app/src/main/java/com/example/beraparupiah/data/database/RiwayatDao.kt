package com.example.beraparupiah.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.beraparupiah.data.model.RiwayatDeteksi

@Dao
interface RiwayatDao {

    @Query("SELECT * FROM riwayat_deteksi ORDER BY timestamp DESC")
    fun getAllRiwayat(): LiveData<List<RiwayatDeteksi>>

    @Query("SELECT * FROM riwayat_deteksi WHERE userId = :userId ORDER BY timestamp DESC")
    fun getRiwayatByUser(userId: String): LiveData<List<RiwayatDeteksi>>

    @Query("SELECT * FROM riwayat_deteksi WHERE id = :id")
    suspend fun getRiwayatById(id: Int): RiwayatDeteksi?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(riwayat: RiwayatDeteksi)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(riwayatList: List<RiwayatDeteksi>)

    @Update
    suspend fun update(riwayat: RiwayatDeteksi)

    @Delete
    suspend fun delete(riwayat: RiwayatDeteksi)

    @Query("DELETE FROM riwayat_deteksi")
    suspend fun deleteAll()

    @Query("DELETE FROM riwayat_deteksi WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)

    // Delete multiple items
    @Delete
    suspend fun deleteAll(items: List<RiwayatDeteksi>)
}